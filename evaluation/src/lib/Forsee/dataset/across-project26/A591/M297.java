    private void mknod_inner(String path, CreateMode mode) throws KeeperException, InterruptedException {
        for(int i = 0; i < maxTries; i++) {
            try {
                zk.create(path, null, Ids.OPEN_ACL_UNSAFE, mode);
                break;
            } catch (NodeExistsException e) {
                if (mode != CreateMode.EPHEMERAL) {
                    return;
                }
                Stat stat = zk.exists(path, false);
                if (stat == null) {
                    continue;
                }
                if (stat.getEphemeralOwner() != zk.getSessionId()) {
                    throw e;
                }
                break;
            } catch (ConnectionLossException e) {
                e.printStackTrace();
            }
        }
    }
