
private AsyncProfiler profilerInstance() {
    if (profiler != null) return profiler;
    if (ProfilerAction.load.toString().equals(action)) {
        profiler = AsyncProfiler.getInstance(actionArg);
    }

    if (libPath != null) {
        try {
            File tmpLibFile = File.createTempFile(VmTool.JNI_LIBRARY_NAME, null);
            try (FileInputStream in = new FileInputStream(libPath);
                 FileOutputStream out = new FileOutputStream(tmpLibFile)) {
                IOUtils.copy(in, out);
            }
            libPath = tmpLibFile.getAbsolutePath();
            logger.debug("copy {} to {}", libPath, tmpLibFile);
        } catch (Throwable e) {
            logger.error("try to copy lib error! libPath: {}", libPath, e);
        }
        profiler = AsyncProfiler.getInstance(libPath);
    } else {
        if (!OSUtils.isLinux() && !OSUtils.isMac()) {
            throw new IllegalStateException("Current OS do not support AsyncProfiler, Only support Linux/Mac.");
        }
        throw new IllegalStateException("Can not find libasyncProfiler so, please check the arthas directory.");
    }
    return profiler;
}
