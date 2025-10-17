
private AsyncProfiler profilerInstance() {
    if (profiler != null) {
        return profiler;
    }

    // Handle special load path
    if (ProfilerAction.load.toString().equals(action)) {
        profiler = AsyncProfiler.getInstance(this.actionArg);
    }

    if (libPath != null) {
        File tmpLibFile = null;
        try {
            tmpLibFile = File.createTempFile(VmTool.JNI_LIBRARY_NAME, null);
            try (FileInputStream libInputStream = new FileInputStream(libPath);
                 FileOutputStream tmpLibOutputStream = new FileOutputStream(tmpLibFile)) {
                IOUtils.copy(libInputStream, tmpLibOutputStream);
                libPath = tmpLibFile.getAbsolutePath();
                logger.debug("copy {} to {}", libPath, tmpLibFile);
            }
        } catch (Throwable e) {
            logger.error("try to copy lib error! libPath: {}", libPath, e);
        }
        profiler = AsyncProfiler.getInstance(libPath);
    } else {
        if (OSUtils.isLinux() || OSUtils.isMac()) {
            throw new IllegalStateException("Can not find libasyncProfiler so, please check the arthas directory.");
        } else {
            throw new IllegalStateException("Current OS do not support AsyncProfiler, Only support Linux/Mac.");
        }
    }

    return profiler;
}
