    synchronized public void processResult(int rc, String path, Object ctx,
            List<String> children) {
        if (rc != KeeperException.Code.OK.intValue()) {
            zk.getChildren(statusNode, this, this, null);
            return;
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("Got " + children + " children from " + path);
        }
        Map<String, HashSet<Assigned>> newAssignments = new HashMap<String, HashSet<Assigned>>();
        for(String c: children) {
            HashSet<Assigned> a = assignments.remove(c);
            if (a != null) {
                newAssignments.put(c, a);
            } else {
                newAssignments.put(c, new HashSet<Assigned>());
            }
        }
        // Clean up the dead machines
        for(String dead: assignments.keySet()) {
            try {
                removeInstance(dead);
            } catch (KeeperException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        assignments = newAssignments;
    }
