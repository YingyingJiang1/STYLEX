    private void distributedConfigureServers(int count) throws IOException {
        StringBuilder sbClient = new StringBuilder();
        StringBuilder sbServer = new StringBuilder();
        try {
            for(int i = 0; i < count; i++) {
                String r[] = QuorumPeerInstance.createServer(im, i);
                if (i > 0) {
                    sbClient.append(',');
                    sbServer.append(',');
                }
                sbClient.append(r[0]); // r[0] == "host:clientPort"
                sbServer.append(r[1]); // r[1] == "host:leaderPort:leaderElectionPort"
                sbServer.append(";"+(r[0].split(":"))[1]); // Appending ";clientPort"
            }
            serverHostPort = sbClient.toString();
            quorumHostPort = sbServer.toString();
        } catch(Exception e) {
            IOException ioe = new IOException(e.getMessage());
            ioe.setStackTrace(e.getStackTrace());
            throw ioe;
        }
    }
