        void drain() {
            if (wip.getAndIncrement() != 0) {
                return;
            }

            int missed = 1;

            SimpleQueue<T> q = queue;

            int upstreamConsumed = consumed;
            int localLimit = limit;
            boolean canRequest = sourceMode != QueueSubscription.SYNC;
            AtomicReference<MulticastSubscription<T>[]> subs = subscribers;

            MulticastSubscription<T>[] array = subs.get();

            outer:
            for (;;) {

                int n = array.length;

                if (q != null && n != 0) {
                    long r = Long.MAX_VALUE;

                    for (MulticastSubscription<T> ms : array) {
                        long u = ms.get() - ms.emitted;
                        if (u != Long.MIN_VALUE) {
                            if (r > u) {
                                r = u;
                            }
                        } else {
                            n--;
                        }
                    }

                    if (n == 0) {
                        r = 0;
                    }

                    while (r != 0) {
                        if (isDisposed()) {
                            q.clear();
                            return;
                        }

                        boolean d = done;

                        if (d && !delayError) {
                            Throwable ex = error;
                            if (ex != null) {
                                errorAll(ex);
                                return;
                            }
                        }

                        T v;

                        try {
                            v = q.poll();
                        } catch (Throwable ex) {
                            Exceptions.throwIfFatal(ex);
                            SubscriptionHelper.cancel(upstream);
                            errorAll(ex);
                            return;
                        }

                        boolean empty = v == null;

                        if (d && empty) {
                            Throwable ex = error;
                            if (ex != null) {
                                errorAll(ex);
                            } else {
                                completeAll();
                            }
                            return;
                        }

                        if (empty) {
                            break;
                        }

                        boolean subscribersChange = false;

                        for (MulticastSubscription<T> ms : array) {
                            long msr = ms.get();
                            if (msr != Long.MIN_VALUE) {
                                if (msr != Long.MAX_VALUE) {
                                    ms.emitted++;
                                }
                                ms.downstream.onNext(v);
                            } else {
                                subscribersChange = true;
                            }
                        }

                        r--;

                        if (canRequest && ++upstreamConsumed == localLimit) {
                            upstreamConsumed = 0;
                            upstream.get().request(localLimit);
                        }

                        MulticastSubscription<T>[] freshArray = subs.get();
                        if (subscribersChange || freshArray != array) {
                            array = freshArray;
                            continue outer;
                        }
                    }

                    if (r == 0) {
                        if (isDisposed()) {
                            q.clear();
                            return;
                        }

                        boolean d = done;

                        if (d && !delayError) {
                            Throwable ex = error;
                            if (ex != null) {
                                errorAll(ex);
                                return;
                            }
                        }

                        if (d && q.isEmpty()) {
                            Throwable ex = error;
                            if (ex != null) {
                                errorAll(ex);
                            } else {
                                completeAll();
                            }
                            return;
                        }
                    }
                }

                consumed = upstreamConsumed;
                missed = wip.addAndGet(-missed);
                if (missed == 0) {
                    break;
                }
                if (q == null) {
                    q = queue;
                }
                array = subs.get();
            }
        }
