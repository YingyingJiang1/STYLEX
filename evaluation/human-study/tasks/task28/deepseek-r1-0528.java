
public static String renderEnhancerAffect(EnhancerAffectVO affectVO) {
    final StringBuilder infoSB = new StringBuilder();

    Optional.ofNullable(affectVO.getClassDumpFiles())
            .ifPresent(files -> files.forEach(file -> 
                infoSB.append("[dump: ").append(file).append("]\n")
            ));

    Optional.ofNullable(affectVO.getMethods())
            .ifPresent(methods -> methods.forEach(method -> 
                infoSB.append("[Affect method: ").append(method).append("]\n")
            ));

    infoSB.append(format(
        "Affect(class count: %d , method count: %d) cost in %s ms, listenerId: %d",
        affectVO.getClassCount(),
        affectVO.getMethodCount(),
        affectVO.getCost(),
        affectVO.getListenerId()
    ));

    Optional.ofNullable(affectVO.getOverLimitMsg())
            .filter(msg -> !msg.isEmpty())
            .ifPresent(msg -> infoSB.append('\n').append(msg));

    Optional.ofNullable(affectVO.getThrowable())
            .ifPresent(t -> 
                infoSB.append("\nEnhance error! exception: ").append(t)
            );

    return infoSB.append('\n').toString();
}
