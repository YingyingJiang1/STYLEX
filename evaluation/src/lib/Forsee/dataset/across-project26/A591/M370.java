    @Override
    void cleanup() {
        if (sockKey != null) {
            SocketChannel sock = (SocketChannel) sockKey.channel();
            sockKey.cancel();
            try {
                sock.socket().shutdownInput();
            } catch (IOException e) {
                LOG.debug("Ignoring exception during shutdown input", e);
            }
            try {
                sock.socket().shutdownOutput();
            } catch (IOException e) {
                LOG.debug("Ignoring exception during shutdown output", e);
            }
            try {
                sock.socket().close();
            } catch (IOException e) {
                LOG.debug("Ignoring exception during socket close", e);
            }
            try {
                sock.close();
            } catch (IOException e) {
                LOG.debug("Ignoring exception during channel close", e);
            }
        }
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            LOG.debug("SendThread interrupted during sleep, ignoring");
        }
        sockKey = null;
    }
