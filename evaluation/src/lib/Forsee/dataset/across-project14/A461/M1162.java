        @Override
        void createFirstWindow() {
            if (!downstreamCancelled.get()) {
                if (requested.get() != 0L) {
                    windowCount.getAndIncrement();
                    window = UnicastProcessor.create(bufferSize, windowRunnable);

                    emitted = 1;

                    FlowableWindowSubscribeIntercept<T> intercept = new FlowableWindowSubscribeIntercept<>(window);
                    downstream.onNext(intercept);

                    timer.replace(scheduler.schedulePeriodicallyDirect(this, timespan, timespan, unit));

                    if (intercept.tryAbandon()) {
                        window.onComplete();
                    }

                    upstream.request(Long.MAX_VALUE);
                } else {
                    upstream.cancel();
                    downstream.onError(missingBackpressureMessage(emitted));

                    cleanupResources();
                    upstreamCancelled = true;
                }
            }
        }
