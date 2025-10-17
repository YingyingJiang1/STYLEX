
public static int startArthasClient(String arthasHomeDir, List<String> telnetArgs, OutputStream out) throws Throwable {
    URLClassLoader classLoader = new URLClassLoader(
        new URL[]{new File(arthasHomeDir, "arthas-client.jar").toURI().toURL()}
    );
    Class<?> telnetConsoleClass = classLoader.loadClass("com.taobao.arthas.client.TelnetConsole");
    Method processMethod = telnetConsoleClass.getMethod("process", String[].class);

    PrintStream originSysOut = System.out;
    PrintStream originSysErr = System.err;
    PrintStream newOut = new PrintStream(out);
    PrintStream newErr = new PrintStream(out);

    ClassLoader tccl = Thread.currentThread().getContextClassLoader();
    try {
        System.setOut(newOut);
        System.setErr(newErr);
        Thread.currentThread().setContextClassLoader(classLoader);
        
        return (Integer) processMethod.invoke(null, new Object[]{telnetArgs.toArray(new String[0])});
    } catch (Throwable e) {
        Throwable cause = e.getCause();
        if (cause instanceof IOException || cause instanceof InterruptedException) {
            return STATUS_ERROR;
        } else {
            AnsiLog.error("process error: {}", cause.toString());
            AnsiLog.error(cause);
            return STATUS_EXEC_ERROR;
        }
    } finally {
        Thread.currentThread().setContextClassLoader(tccl);
        System.setOut(originSysOut);
        System.setErr(originSysErr);
        newOut.flush();
        newErr.flush();
    }
}
