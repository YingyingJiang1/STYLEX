        @Override
        void drain() {
            if (getAndIncrement() != 0) {
                return;
            }

            int missed = 1;
            Subscriber<? super R> downstream = this.downstream;
            ErrorMode errorMode = this.errorMode;
            SimpleQueue<T> queue = this.queue;
            AtomicThrowable errors = this.errors;
            AtomicLong requested = this.requested;
            int limit = prefetch - (prefetch >> 1);
            boolean syncFused = this.syncFused;

            for (;;) {

                for (;;) {
                    if (cancelled) {
                        queue.clear();
                        item = null;
                        break;
                    }

                    int s = state;

                    if (errors.get() != null) {
                        if (errorMode == ErrorMode.IMMEDIATE
                                || (errorMode == ErrorMode.BOUNDARY && s == STATE_INACTIVE)) {
                            queue.clear();
                            item = null;
                            errors.tryTerminateConsumer(downstream);
                            return;
                        }
                    }

                    if (s == STATE_INACTIVE) {
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

                        if (empty) {
                            break;
                        }

                        if (!syncFused) {
                            int c = consumed + 1;
                            if (c == limit) {
                                consumed = 0;
                                upstream.request(limit);
                            } else {
                                consumed = c;
                            }
                        }

                        SingleSource<? extends R> ss;

                        try {
                            ss = Objects.requireNonNull(mapper.apply(v), "The mapper returned a null SingleSource");
                        } catch (Throwable ex) {
                            Exceptions.throwIfFatal(ex);
                            upstream.cancel();
                            queue.clear();
                            errors.tryAddThrowableOrReport(ex);
                            errors.tryTerminateConsumer(downstream);
                            return;
                        }

                        state = STATE_ACTIVE;
                        ss.subscribe(inner);
                        break;
                    } else if (s == STATE_RESULT_VALUE) {
                        long e = emitted;
                        if (e != requested.get()) {
                            R w = item;
                            item = null;

                            downstream.onNext(w);

                            emitted = e + 1;
                            state = STATE_INACTIVE;
                        } else {
                            break;
                        }
                    } else {
                        break;
                    }
                }

                missed = addAndGet(-missed);
                if (missed == 0) {
                    break;
                }
            }
        }
