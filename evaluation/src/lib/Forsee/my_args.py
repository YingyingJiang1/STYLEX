import os

class Args:
    def __init__(self, **kwargs):
        self.log_dir = '/data/jyy/style/code-style-transformation/experiment/src/evaluation/Forsee/logs'
        self.log_file = os.path.join(self.log_dir, 'common.log')
        
    def set_log_file(self, file_name):
        self.log_file = os.path.join(self.log_dir, file_name)
        
my_args = Args()
