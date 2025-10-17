        void drainSync() {
            int missed = 1;

            SimpleQueue<T> q = queue;
            Subscriber<? super T>[] a = this.subscribers;
            AtomicLongArray r = this.requests;
            long[] e = this.emissions;
            int n = e.length;
            int idx = index;

            for (;;) {

                int notReady = 0;

                for (;;) {
                    if (cancelled) {
                        q.clear();
                        return;
                    }

                    boolean empty = q.isEmpty();

                    if (empty) {
                        for (Subscriber<? super T> s : a) {
                            s.onComplete();
                        }
                        return;
                    }

                    long requestAtIndex = r.get(idx);
                    long emissionAtIndex = e[idx];
                    if (requestAtIndex != emissionAtIndex && r.get(n + idx) == 0) {

                        T v;

                        try {
                            v = q.poll();
                        } catch (Throwable ex) {
                            Exceptions.throwIfFatal(ex);
                            upstream.cancel();
                            for (Subscriber<? super T> s : a) {
                                s.onError(ex);
                            }
                            return;
                        }

                        if (v == null) {
                            for (Subscriber<? super T> s : a) {
                                s.onComplete();
                            }
                            return;
                        }

                        a[idx].onNext(v);

                        e[idx] = emissionAtIndex + 1;

                        notReady = 0;
                    } else {
                        notReady++;
                    }

                    idx++;
                    if (idx == n) {
                        idx = 0;
                    }

                    if (notReady == n) {
                        break;
                    }
                }

                int w = get();
                if (w == missed) {
                    index = idx;
                    missed = addAndGet(-missed);
                    if (missed == 0) {
                        break;
                    }
                } else {
                    missed = w;
                }
            }
        }
