    @Override
    public String toString() {
        String state;
        Object o = get(FUTURE_INDEX);
        if (o == DONE) {
            state = "Finished";
        } else if (o == SYNC_DISPOSED) {
            state = "Disposed(Sync)";
        } else if (o == ASYNC_DISPOSED) {
            state = "Disposed(Async)";
        } else {
            o = get(THREAD_INDEX);
            if (o == null) {
                state = "Waiting";
            } else {
                state = "Running on " + o;
            }
        }

        return getClass().getSimpleName() + "[" + state + "]";
    }
