        void drain() {

            if (getAndIncrement() != 0) {
                return;
            }

            final Subscriber<? super R> a = downstream;
            final ZipSubscriber<T, R>[] qs = subscribers;
            final int n = qs.length;
            Object[] values = current;

            int missed = 1;

            for (;;) {

                long r = requested.get();
                long e = 0L;

                while (r != e) {

                    if (cancelled) {
                        return;
                    }

                    if (!delayErrors && errors.get() != null) {
                        cancelAll();
                        errors.tryTerminateConsumer(a);
                        return;
                    }

                    boolean empty = false;

                    for (int j = 0; j < n; j++) {
                        ZipSubscriber<T, R> inner = qs[j];
                        if (values[j] == null) {
                            boolean d = inner.done;
                            SimpleQueue<T> q = inner.queue;
                            T v = null;
                            try {

                                v = q != null ? q.poll() : null;
                            } catch (Throwable ex) {
                                Exceptions.throwIfFatal(ex);

                                errors.tryAddThrowableOrReport(ex);
                                if (!delayErrors) {
                                    cancelAll();
                                    errors.tryTerminateConsumer(a);
                                    return;
                                }
                                d = true;
                            }

                            boolean sourceEmpty = v == null;
                            if (d && sourceEmpty) {
                                cancelAll();
                                errors.tryTerminateConsumer(a);
                                return;
                            }
                            if (!sourceEmpty) {
                                values[j] = v;
                            } else {
                                empty = true;
                            }
                        }
                    }

                    if (empty) {
                        break;
                    }

                    R v;

                    try {
                        v = Objects.requireNonNull(zipper.apply(values.clone()), "The zipper returned a null value");
                    } catch (Throwable ex) {
                        Exceptions.throwIfFatal(ex);
                        cancelAll();
                        errors.tryAddThrowableOrReport(ex);
                        errors.tryTerminateConsumer(a);
                        return;
                    }

                    a.onNext(v);

                    e++;

                    Arrays.fill(values, null);
                }

                if (r == e) {
                    if (cancelled) {
                        return;
                    }

                    if (!delayErrors && errors.get() != null) {
                        cancelAll();
                        errors.tryTerminateConsumer(a);
                        return;
                    }

                    for (int j = 0; j < n; j++) {
                        ZipSubscriber<T, R> inner = qs[j];
                        if (values[j] == null) {
                            boolean d = inner.done;
                            SimpleQueue<T> q = inner.queue;
                            T v = null;
                            try {
                                v = q != null ? q.poll() : null;
                            } catch (Throwable ex) {
                                Exceptions.throwIfFatal(ex);
                                errors.tryAddThrowableOrReport(ex);
                                if (!delayErrors) {
                                    cancelAll();
                                    errors.tryTerminateConsumer(a);
                                    return;
                                }
                                d = true;
                            }
                            boolean empty = v == null;
                            if (d && empty) {
                                cancelAll();
                                errors.tryTerminateConsumer(a);
                                return;
                            }
                            if (!empty) {
                                values[j] = v;
                            }
                        }
                    }

                }

                if (e != 0L) {

                    for (ZipSubscriber<T, R> inner : qs) {
                        inner.request(e);
                    }

                    if (r != Long.MAX_VALUE) {
                        requested.addAndGet(-e);
                    }
                }

                missed = addAndGet(-missed);
                if (missed == 0) {
                    break;
                }
            }
        }
