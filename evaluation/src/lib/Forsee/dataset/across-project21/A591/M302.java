    @Override
    public void processResult(int rc, String path, Object ctx, List<String> children) {
        if (rc != KeeperException.Code.OK.intValue()) {
            // try it again
            zk.getChildren(assignmentsNode, true, this, null);
            return;
        }
        Map<String, Instance> newList = new HashMap<String, Instance>();
        // check for differences
        Stat stat = new Stat();
        for(String child: children) {
            Instance i = instances.remove(child);
            if (i == null) {
                // Start up a new instance
                byte[] data = null;
                String myNode = assignmentsNode + '/' + child;
                while(true) {
                    try {
                        data = zk.getData(myNode, true, stat);
                        break;
                    } catch (NoNodeException e) {
                        // The node doesn't exist anymore, so skip it
                        break;
                    } catch (KeeperException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        return;
                    }
                }
                if (data != null) {
                    String instanceSpec = new String(data);
                    int spaceIndex = instanceSpec.indexOf(' ');
                    String clazz;
                    String conf;
                    if (spaceIndex == -1) {
                        clazz = instanceSpec;
                        conf = null;
                    } else {
                        clazz = instanceSpec.substring(0, spaceIndex);
                        conf = instanceSpec.substring(spaceIndex+1);
                    }
                    try {
                        Class<?> c = Class.forName(clazz);
                        i = (Instance) c.getConstructor().newInstance();
                        Reporter reporter = new MyReporter(child);
                        i.setReporter(reporter);
                        i.configure(conf);
                        i.start();
                        newList.put(child, i);
                        int ver = stat.getVersion();
                        Instance myInstance = i;
                        DataCallback dc = new MyDataCallback(myNode, myInstance, ver);
                        Watcher watcher = new MyWatcher(myNode, dc);
                        zk.getData(myNode, watcher, dc, watcher);
                    } catch (Exception e) {
                        LOG.warn("Skipping " + child, e);
                        if (e.getCause() != null) {
                            LOG.warn("Caused by", e.getCause());
                        }
                    }

                }
            } else {
                // just move it to the new list
                newList.put(child, i);
            }
        }
        // kill anything that was removed for the children
        for(Map.Entry<String,Instance> i: instances.entrySet()) {
            i.getValue().stop();
            try {
                rmnod(reportsNode + '/' + i.getKey());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (KeeperException e) {
                e.printStackTrace();
            }
        }
        instances = newList;
    }
