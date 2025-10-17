    public void reconfigureInstance(String name, String params) throws NoAssignmentException, InterruptedException, KeeperException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Reconfiguring " + name + " with " + params);
        }
        Assigned assigned = instanceToAssignment.get(name);
        if (assigned == null) {
            throw new NoAssignmentException();
        }
        KeeperException lastException = null;
        for(int i = 0; i < maxTries; i++) {
            try {
                zk.setData(assignmentsNode + '/' + assigned.container + '/' + name, ("update " + params).getBytes(), -1);
                break;
            } catch (ConnectionLossException e) {
                lastException = e;
            }
        }
        if (lastException != null) {
            throw lastException;
        }
    }
