
public static AdminServer createAdminServer() {
    if (!"false".equals(System.getProperty("zookeeper.admin.enableServer"))) {
        try {
            Class<?> jettyAdminServerC = Class.forName(
                    "org.apache.zookeeper.server.admin.JettyAdminServer");
            Object adminServer = jettyAdminServerC.getConstructor().newInstance();
            return (AdminServer) adminServer;
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | 
                 InvocationTargetException | NoSuchMethodException e) {
            LOG.warn("Unable to start JettyAdminServer", e);
        } catch (NoClassDefFoundError e) {
            LOG.warn("Unable to load jetty, not starting JettyAdminServer", e);
        }
    }
    return new DummyAdminServer();
}
