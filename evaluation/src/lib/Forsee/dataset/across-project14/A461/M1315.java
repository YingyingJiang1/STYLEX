        void drainLoop() {
            int missed = 1;
            Subscriber<? super R> a = downstream;
            AtomicInteger n = active;
            AtomicReference<SpscLinkedArrayQueue<R>> qr = queue;

            for (;;) {
                long r = requested.get();
                long e = 0L;

                while (e != r) {
                    if (cancelled) {
                        clear();
                        return;
                    }

                    if (!delayErrors) {
                        Throwable ex = errors.get();
                        if (ex != null) {
                            clear();
                            errors.tryTerminateConsumer(a);
                            return;
                        }
                    }

                    boolean d = n.get() == 0;
                    SpscLinkedArrayQueue<R> q = qr.get();
                    R v = q != null ? q.poll() : null;
                    boolean empty = v == null;

                    if (d && empty) {
                        errors.tryTerminateConsumer(a);
                        return;
                    }

                    if (empty) {
                        break;
                    }

                    a.onNext(v);

                    e++;
                }

                if (e == r) {
                    if (cancelled) {
                        clear();
                        return;
                    }

                    if (!delayErrors) {
                        Throwable ex = errors.get();
                        if (ex != null) {
                            clear();
                            errors.tryTerminateConsumer(a);
                            return;
                        }
                    }

                    boolean d = n.get() == 0;
                    SpscLinkedArrayQueue<R> q = qr.get();
                    boolean empty = q == null || q.isEmpty();

                    if (d && empty) {
                        errors.tryTerminateConsumer(a);
                        return;
                    }
                }

                if (e != 0L) {
                    BackpressureHelper.produced(requested, e);
                    if (maxConcurrency != Integer.MAX_VALUE) {
                        upstream.request(e);
                    }
                }

                missed = addAndGet(-missed);
                if (missed == 0) {
                    break;
                }
            }
        }
