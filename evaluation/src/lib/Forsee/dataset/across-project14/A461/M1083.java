        void drain() {
            if (getAndIncrement() != 0) {
                return;
            }

            int missed = 1;
            Subscriber<? super R> a = downstream;
            SimplePlainQueue<R> q = queue;
            int lim = limit;
            int c = consumed;

            for (;;) {

                long r = requested.get();
                long e = 0L;

                while (e != r) {
                    if (cancelled) {
                        q.clear();
                        return;
                    }
                    boolean d = done;

                    if (d) {
                        Throwable ex = error;
                        if (ex != null) {
                            q.clear();
                            a.onError(ex);
                            return;
                        }
                    }

                    R v = q.poll();
                    boolean empty = v == null;

                    if (d && empty) {
                        a.onComplete();
                        return;
                    }

                    if (empty) {
                        break;
                    }

                    a.onNext(v);

                    e++;
                    if (++c == lim) {
                        c = 0;
                        upstream.request(lim);
                    }
                }

                if (e == r) {
                    if (done) {
                        Throwable ex = error;
                        if (ex != null) {
                            q.clear();
                            a.onError(ex);
                            return;
                        }
                        if (q.isEmpty()) {
                            a.onComplete();
                            return;
                        }
                    }
                }

                if (e != 0L) {
                    BackpressureHelper.produced(requested, e);
                }

                consumed = c;
                missed = addAndGet(-missed);
                if (missed == 0) {
                    break;
                }
            }
        }
