        @Override
        void drain() {
            if (getAndIncrement() != 0) {
                return;
            }

            ErrorMode errorMode = this.errorMode;
            SimpleQueue<T> queue = this.queue;
            AtomicThrowable errors = this.errors;
            boolean syncFused = this.syncFused;

            do {
                if (cancelled) {
                    queue.clear();
                    return;
                }

                if (errors.get() != null) {
                    if (errorMode == ErrorMode.IMMEDIATE
                            || (errorMode == ErrorMode.BOUNDARY && !active)) {
                        queue.clear();
                        errors.tryTerminateConsumer(downstream);
                        return;
                    }
                }

                if (!active) {

                    boolean d = done;
                    T v;
                    try {
                        v = queue.poll();
                    } catch (Throwable ex) {
                        Exceptions.throwIfFatal(ex);
                        upstream.cancel();
                        errors.tryAddThrowableOrReport(ex);
                        errors.tryTerminateConsumer(downstream);
                        return;
                    }
                    boolean empty = v == null;

                    if (d && empty) {
                        errors.tryTerminateConsumer(downstream);
                        return;
                    }

                    if (!empty) {

                        int limit = prefetch - (prefetch >> 1);

                        if (!syncFused) {
                            int c = consumed + 1;
                            if (c == limit) {
                                consumed = 0;
                                upstream.request(limit);
                            } else {
                                consumed = c;
                            }
                        }

                        CompletableSource cs;

                        try {
                            cs = Objects.requireNonNull(mapper.apply(v), "The mapper returned a null CompletableSource");
                        } catch (Throwable ex) {
                            Exceptions.throwIfFatal(ex);
                            queue.clear();
                            upstream.cancel();
                            errors.tryAddThrowableOrReport(ex);
                            errors.tryTerminateConsumer(downstream);
                            return;
                        }
                        active = true;
                        cs.subscribe(inner);
                    }
                }
            } while (decrementAndGet() != 0);
        }
