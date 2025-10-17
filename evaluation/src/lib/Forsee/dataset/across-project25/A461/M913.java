        void drainAsync() {
            int missed = 1;

            SimpleQueue<T> q = queue;
            Subscriber<? super T>[] a = this.subscribers;
            AtomicLongArray r = this.requests;
            long[] e = this.emissions;
            int n = e.length;
            int idx = index;
            int consumed = produced;

            for (;;) {

                int notReady = 0;

                for (;;) {
                    if (cancelled) {
                        q.clear();
                        return;
                    }

                    boolean d = done;
                    if (d) {
                        Throwable ex = error;
                        if (ex != null) {
                            q.clear();
                            for (Subscriber<? super T> s : a) {
                                s.onError(ex);
                            }
                            return;
                        }
                    }

                    boolean empty = q.isEmpty();

                    if (d && empty) {
                        for (Subscriber<? super T> s : a) {
                            s.onComplete();
                        }
                        return;
                    }

                    if (empty) {
                        break;
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
                            break;
                        }

                        a[idx].onNext(v);

                        e[idx] = emissionAtIndex + 1;

                        int c = ++consumed;
                        if (c == limit) {
                            consumed = 0;
                            upstream.request(c);
                        }
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
                    produced = consumed;
                    missed = addAndGet(-missed);
                    if (missed == 0) {
                        break;
                    }
                } else {
                    missed = w;
                }
            }
        }
