
    public static AdminServer createAdminServer() {
        if (!"false".equals(System.getProperty("zookeeper.admin.enableServer"))) {
            try {
                final Class<?> jettyAdminServerC = Class.forName("org.apache.zookeeper.server.admin.JettyAdminServer");
                final Object adminServer = jettyAdminServerC.getConstructor().newInstance();
                return (AdminServer) adminServer;
            } catch (final ClassNotFoundException | InstantiationException | IllegalAccessException
                    | InvocationTargetException | NoSuchMethodException e) {
                LOG.warn("Unable to start JettyAdminServer", e);
            } catch (final NoClassDefFoundError e) {
                LOG.warn("Unable to load jetty, not starting JettyAdminServer", e);
            }
        }
        return new DummyAdminServer();
    }
