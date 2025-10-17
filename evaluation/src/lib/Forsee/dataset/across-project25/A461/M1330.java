        @Override
        public void drain() {
            if (getAndIncrement() != 0) {
                return;
            }

            int missed = 1;

            for (;;) {
                SimpleQueue<T> q1 = first.queue;
                SimpleQueue<T> q2 = second.queue;

                if (q1 != null && q2 != null) {
                    for (;;) {
                        if (isDisposed()) {
                            first.clear();
                            second.clear();
                            return;
                        }

                        Throwable ex = errors.get();
                        if (ex != null) {
                            cancelAndClear();

                            errors.tryTerminateConsumer(downstream);
                            return;
                        }

                        boolean d1 = first.done;

                        T a = v1;
                        if (a == null) {
                            try {
                                a = q1.poll();
                            } catch (Throwable exc) {
                                Exceptions.throwIfFatal(exc);
                                cancelAndClear();
                                errors.tryAddThrowableOrReport(exc);
                                errors.tryTerminateConsumer(downstream);
                                return;
                            }
                            v1 = a;
                        }
                        boolean e1 = a == null;

                        boolean d2 = second.done;
                        T b = v2;
                        if (b == null) {
                            try {
                                b = q2.poll();
                            } catch (Throwable exc) {
                                Exceptions.throwIfFatal(exc);
                                cancelAndClear();
                                errors.tryAddThrowableOrReport(exc);
                                errors.tryTerminateConsumer(downstream);
                                return;
                            }
                            v2 = b;
                        }

                        boolean e2 = b == null;

                        if (d1 && d2 && e1 && e2) {
                            downstream.onSuccess(true);
                            return;
                        }
                        if ((d1 && d2) && (e1 != e2)) {
                            cancelAndClear();
                            downstream.onSuccess(false);
                            return;
                        }

                        if (e1 || e2) {
                            break;
                        }

                        boolean c;

                        try {
                            c = comparer.test(a, b);
                        } catch (Throwable exc) {
                            Exceptions.throwIfFatal(exc);
                            cancelAndClear();
                            errors.tryAddThrowableOrReport(exc);
                            errors.tryTerminateConsumer(downstream);
                            return;
                        }

                        if (!c) {
                            cancelAndClear();
                            downstream.onSuccess(false);
                            return;
                        }

                        v1 = null;
                        v2 = null;

                        first.request();
                        second.request();
                    }

                } else {
                    if (isDisposed()) {
                        first.clear();
                        second.clear();
                        return;
                    }

                    Throwable ex = errors.get();
                    if (ex != null) {
                        cancelAndClear();

                        errors.tryTerminateConsumer(downstream);
                        return;
                    }
                }

                missed = addAndGet(-missed);
                if (missed == 0) {
                    break;
                }
            }
        }
