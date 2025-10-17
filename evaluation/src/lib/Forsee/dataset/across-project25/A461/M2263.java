    @SuppressWarnings("unchecked")
    void drain() {
        if (wip.getAndIncrement() != 0) {
            return;
        }

        int missed = 1;
        AtomicReference<MulticastSubscription<T>[]> subs = subscribers;
        int c = consumed;
        int lim = limit;
        int fm = fusionMode;

        outer:
        for (;;) {

            SimpleQueue<T> q = queue;

            if (q != null) {
                MulticastSubscription<T>[] as = subs.get();
                int n = as.length;

                if (n != 0) {
                    long r = -1L;

                    for (MulticastSubscription<T> a : as) {
                        long ra = a.get();
                        if (ra >= 0L) {
                            if (r == -1L) {
                                r = ra - a.emitted;
                            } else {
                                r = Math.min(r, ra - a.emitted);
                            }
                        }
                    }

                    while (r > 0L) {
                        MulticastSubscription<T>[] bs = subs.get();

                        if (bs == TERMINATED) {
                            q.clear();
                            return;
                        }

                        if (as != bs) {
                            continue outer;
                        }

                        boolean d = done;

                        T v;

                        try {
                            v = q.poll();
                        } catch (Throwable ex) {
                            Exceptions.throwIfFatal(ex);
                            SubscriptionHelper.cancel(upstream);
                            d = true;
                            v = null;
                            error = ex;
                            done = true;
                        }
                        boolean empty = v == null;

                        if (d && empty) {
                            Throwable ex = error;
                            if (ex != null) {
                                for (MulticastSubscription<T> inner : subs.getAndSet(TERMINATED)) {
                                    inner.onError(ex);
                                }
                            } else {
                                for (MulticastSubscription<T> inner : subs.getAndSet(TERMINATED)) {
                                    inner.onComplete();
                                }
                            }
                            return;
                        }

                        if (empty) {
                            break;
                        }

                        for (MulticastSubscription<T> inner : as) {
                            inner.onNext(v);
                        }

                        r--;

                        if (fm != QueueSubscription.SYNC) {
                            if (++c == lim) {
                                c = 0;
                                upstream.get().request(lim);
                            }
                        }
                    }

                    if (r == 0) {
                        MulticastSubscription<T>[] bs = subs.get();

                        if (bs == TERMINATED) {
                            q.clear();
                            return;
                        }

                        if (as != bs) {
                            continue;
                        }

                        if (done && q.isEmpty()) {
                            Throwable ex = error;
                            if (ex != null) {
                                for (MulticastSubscription<T> inner : subs.getAndSet(TERMINATED)) {
                                    inner.onError(ex);
                                }
                            } else {
                                for (MulticastSubscription<T> inner : subs.getAndSet(TERMINATED)) {
                                    inner.onComplete();
                                }
                            }
                            return;
                        }
                    }
                }
            }

            consumed = c;
            missed = wip.addAndGet(-missed);
            if (missed == 0) {
                break;
            }
        }
    }
