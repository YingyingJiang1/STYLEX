        @Override
        public void run() {
            for (;;) {
                if (cancelled) {
                    return;
                }

                if (!active) {
                    boolean d = done;

                    T v;

                    try {
                        v = queue.poll();
                    } catch (Throwable e) {
                        Exceptions.throwIfFatal(e);
                        upstream.cancel();
                        errors.tryAddThrowableOrReport(e);
                        errors.tryTerminateConsumer(downstream);
                        worker.dispose();
                        return;
                    }

                    boolean empty = v == null;

                    if (d && empty) {
                        downstream.onComplete();
                        worker.dispose();
                        return;
                    }

                    if (!empty) {
                        Publisher<? extends R> p;

                        try {
                            p = Objects.requireNonNull(mapper.apply(v), "The mapper returned a null Publisher");
                        } catch (Throwable e) {
                            Exceptions.throwIfFatal(e);

                            upstream.cancel();
                            errors.tryAddThrowableOrReport(e);
                            errors.tryTerminateConsumer(downstream);
                            worker.dispose();
                            return;
                        }

                        if (sourceMode != QueueSubscription.SYNC) {
                            int c = consumed + 1;
                            if (c == limit) {
                                consumed = 0;
                                upstream.request(c);
                            } else {
                                consumed = c;
                            }
                        }

                        if (p instanceof Supplier) {
                            @SuppressWarnings("unchecked")
                            Supplier<R> supplier = (Supplier<R>) p;

                            R vr;

                            try {
                                vr = supplier.get();
                            } catch (Throwable e) {
                                Exceptions.throwIfFatal(e);
                                upstream.cancel();
                                errors.tryAddThrowableOrReport(e);
                                errors.tryTerminateConsumer(downstream);
                                worker.dispose();
                                return;
                            }

                            if (vr == null || cancelled) {
                                continue;
                            }

                            if (inner.isUnbounded()) {
                                if (tryEnter()) {
                                    downstream.onNext(vr);
                                    if (!compareAndSet(1, 0)) {
                                        errors.tryTerminateConsumer(downstream);
                                        worker.dispose();
                                        return;
                                    }
                                }
                                continue;
                            } else {
                                active = true;
                                inner.setSubscription(new SimpleScalarSubscription<>(vr, inner));
                            }

                        } else {
                            active = true;
                            p.subscribe(inner);
                        }
                    }
                }
                if (wip.decrementAndGet() == 0) {
                    break;
                }
            }
        }
