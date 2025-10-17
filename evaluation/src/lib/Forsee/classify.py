import torch
import fire
import torch.utils.data as torchdata
import pickle
import json
import os
import sys
sys.path.append('../..')


from codesecurity.tasks.code_authorship_attribution.prepare_torch import ForseeDataset
from codesecurity.tasks.code_authorship_attribution.preprocessing import ForseeFeatures, ForseeFeature
from codesecurity.tasks.code_authorship_attribution.training_model import forsee_call
from codesecurity.tasks.code_authorship_attribution import (
    ForseeCachesMetatadata, ForseeSuperParameter)
from main import caa_build_addon_data, caa_forsee
from codesecurity.data.api import pickle_load

import logging

log_dir = r'./logs'

log_file_name = 'my_eval.log'

DEVICE_GPU=torch.device('cuda:0')
DEVICE_CPU=torch.device('cpu')

class MyForseeDataset(ForseeDataset):
    def __init__(self, *args, **kwargs):
        super().__init__(*args, **kwargs)
        
    
    def __getitem__(self, index):
        sample=self.features.samples[index]
        return index, sample.layout,sample.lexical,sample.syntactic,sample.label
    
    
class ClassificationResult:
    '''
    对于transformed codes，self.author_name和self.author_id分别是目标风格作者的姓名和id， self.problem_id组成方式为：输入程序作者名字_输入程序问题id_目标风格程序问题id
    '''
    def __init__(self,fid, author_name, author_id, author_ids, confidence_list, top1id, top5id):
        self.fid=fid
        self.author_name=author_name # authorship of fid
        self.author_id=author_id
        self.author_ids=author_ids
        self.confidence_list = confidence_list
        self.top1id = top1id
        self.top5id = top5id
        
    def get_pair_key(self):
        target_author_name = self.author_name
        if len(self.fid.split('_')) < 3:
            return (self.author_name, self.fid)
        src_author_name, src_problem_id, target_problem_id = self.fid.split('_')
        return (src_author_name, src_problem_id, target_author_name, target_problem_id)
    

    
def do_classify(model:torch.nn.Module,data:torchdata.DataLoader,device,loss_func=torch.nn.CrossEntropyLoss(),model_call_handle=None) -> list[ClassificationResult]:

    logging.basicConfig(
    filename=os.path.join(log_dir, log_file_name) ,
    level=logging.INFO,  
    format="%(asctime)s - %(levelname)s - %(message)s",
    datefmt="%Y-%m-%d %H:%M:%S"
    )

    logger = logging.getLogger()


    if model_call_handle is None:
        model_call_handle=lambda model,x:model(*x)

    total_number=0
    total_test_loss=0
    total_accuracy=0
    total_acc5=0
    number_of_iter=0
    model.eval()
    
    result = []
    try:
        with torch.no_grad():
            for e in data:
                # 去除索引
                indices = e[0].tolist()
                e = e[1:]
                
                y=e[-1]
                y=y.to(device)
                y_hat=model_call_handle(model,e[:-1])
                # y_hat=torch.softmax(y_hat,1)
                total_number+=y.size(0)

                if len(y.size())==1:
                    y=torch.eye(y_hat.size(-1),device=device)[y]

                loss= loss_func(y_hat,y.double())
                total_test_loss = total_test_loss + loss.item()
                class_number=y_hat.size(-1)
                confidences,author_ids=torch.topk(y_hat,class_number,dim=-1)
                
                _,maxk=torch.topk(y_hat,min(5,class_number),dim=-1)
                total_accuracy+=(y.argmax(1).view(-1,1) == maxk[:,0:1]).sum().item()
                total_acc5+=(y.argmax(1).view(-1,1)== maxk).sum().item()

                confidences, author_ids = confidences.tolist(), author_ids.tolist()
                features :ForseeFeatures = data.dataset.features
                for i in range(len(indices)):
                    index = indices[i]
                    feature:ForseeFeature = features.samples[index]
                    author_id = feature.label
                    fid = os.path.basename(feature.origin_paths).replace('.java','')
                    author_name = os.path.basename(os.path.dirname(feature.origin_paths))
                    ground_truth = features.id_mapping[author_name]
                    assert ground_truth == author_id
                    top1id = author_ids[i][0]
                    top5id = author_ids[i][0:5]
                    result.append(ClassificationResult(fid, author_name, author_id, author_ids[i], confidences[i], top1id, top5id))
                    author_rank = list(author_ids[i]).index(author_id) + 1
                    print(f"Classification result of {author_name}/{fid}: top1 success:{top1id == author_id}. Author rank: {author_rank}/{author_rank / class_number * 100:.2}%")
                    logger.info(f"Classification result of {author_name}/{fid}:{top1id == author_id}. Author rank: {author_rank}/{author_rank / class_number * 100:.2}%")
        print(f'{total_number} files have been classified. top1 acc:{total_accuracy/total_number} top5 acc: {total_acc5/total_number}')
        logger.info(f'{total_number} files have been classified. top1 acc:{total_accuracy/total_number} top5 acc: {total_acc5/total_number}')
    except Exception as e:
        print("Error during classification", e)
       
    return result

        

def classify_dataset(model_dir, dataset_dir, caches_dir, result_path, sp='java40', prefer=None, device='gpu', use_caches=True, include_submodels=True, **kwargs):
    global log_file_name
    log_file_name= os.path.basename(result_path).split('_')[0] + '_'
    
    result_dir = os.path.dirname(result_path)
    os.makedirs(result_dir,exist_ok=True)
        
    if prefer is None:
        prefer=[1.5,1,3]
        # 1,1,1.5
        # 1.5,1,1.5
        # 3,1,3
    meta=ForseeCachesMetatadata.auto(model_dir)
    sp=ForseeSuperParameter.instance(sp)
    
    if device=='gpu':
        device=DEVICE_GPU
    else:
        device=DEVICE_CPU
    
    
    data=caa_build_addon_data(dataset_dir,caches_dir,meta,sp,use_caches=use_caches)
    my_data = MyForseeDataset(data.features)
    model=caa_forsee(meta,sp,my_data.class_number,device,prefer=prefer)
    
    my_data=torchdata.DataLoader(my_data,batch_size=64)
    result = do_classify(model,my_data,device,model_call_handle=forsee_call)
    
    with open(result_path, 'w') as file:
        for record in result:
            file.write(json.dumps(record.__dict__) + '\n')
            
            
def save_user_id_map(output_path, data_file="caches/my_training_dataset/data.pt"):
    with open(data_file,'rb') as f:
            data=pickle.load(f)
            
    with open(output_path,'w', encoding='utf-8') as f:
        json.dump(data.addon, f)



if __name__=="__main__":
    # save_user_id_map(os.path.join(RESULT_DIR, 'forsee_eval', 'user_id_map.json'))
    fire.Fire()

    

    
