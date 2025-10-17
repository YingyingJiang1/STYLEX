
private ApiResponse processAsyncExecRequest(ApiRequest apiRequest, Session session) {
    String commandLine = apiRequest.getCommand();
    Map<String, Object> body = new TreeMap<>();
    body.put("command", commandLine);

    ApiResponse response = new ApiResponse()
        .setSessionId(session.getSessionId())
        .setBody(body);

    if (!session.tryLock()) {
        return response.setState(ApiState.REFUSED)
                      .setMessage("Another command is executing.");
    }

    int lock = session.getLock();
    try {
        Job foregroundJob = session.getForegroundJob();
        if (foregroundJob != null) {
            logger.info("Another job is running, jobId: {}", foregroundJob.id());
            return response.setState(ApiState.REFUSED)
                          .setMessage("Another job is running.");
        }

        Job job = createJob(commandLine, session, session.getResultDistributor());
        body.put("jobId", job.id());
        body.put("jobStatus", job.status());
        response.setState(ApiState.SCHEDULED);

        CommandRequestModel commandRequestModel = new CommandRequestModel(commandLine, response.getState())
            .setJobId(job.id());
        
        Optional.ofNullable(session.getResultDistributor())
                .ifPresent(distributor -> distributor.appendResult(commandRequestModel));
                
        session.setForegroundJob(job);
        updateSessionInputStatus(session, InputStatus.ALLOW_INTERRUPT);
        job.run();

        return response;
    } catch (Throwable e) {
        logger.error("Async exec command failed: {}, command: {}", e.getMessage(), commandLine, e);
        response.setState(ApiState.FAILED)
                .setMessage("Async exec command failed:" + e.getMessage());
                
        new CommandRequestModel(commandLine, response.getState(), response.getMessage())
            .appendTo(session.getResultDistributor());
            
        return response;
    } finally {
        if (session.getLock() == lock) {
            session.unLock();
        }
    }
}
