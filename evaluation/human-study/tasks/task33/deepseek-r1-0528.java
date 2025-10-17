
private static Map<String, Object> doGetLoggerInfo(Logger logger) {
    final Map<String, Object> info = new HashMap<>();
    info.put(LoggerHelper.name, logger.getName());
    info.put(LoggerHelper.clazz, logger.getClass());
    
    final CodeSource codeSource = logger.getClass().getProtectionDomain().getCodeSource();
    if (codeSource != null) {
        info.put(LoggerHelper.codeSource, codeSource.getLocation());
    }
    info.put(LoggerHelper.additivity, logger.getAdditivity());

    final Level level = logger.getLevel();
    final Level effectiveLevel = logger.getEffectiveLevel();
    if (level != null) {
        info.put(LoggerHelper.level, level.toString());
    }
    if (effectiveLevel != null) {
        info.put(LoggerHelper.effectiveLevel, effectiveLevel.toString());
    }

    @SuppressWarnings("unchecked")
    final List<Map<String, Object>> result = doGetLoggerAppenders(logger.getAllAppenders());
    info.put(LoggerHelper.appenders, result);
    return info;
}
