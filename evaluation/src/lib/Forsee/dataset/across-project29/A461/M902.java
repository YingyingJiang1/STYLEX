        @Override
        public void run() {
            int missed = 1;
            int c = consumed;
            SpscArrayQueue<T> q = queue;
            Subscriber<? super T> a = downstream;
            int lim = limit;

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

                            worker.dispose();
                            return;
                        }
                    }

                    T v = q.poll();

                    boolean empty = v == null;

                    if (d && empty) {
                        a.onComplete();

                        worker.dispose();
                        return;
                    }

                    if (empty) {
                        break;
                    }

                    a.onNext(v);

                    e++;

                    int p = ++c;
                    if (p == lim) {
                        c = 0;
                        upstream.request(p);
                    }
                }

                if (e == r) {
                    if (cancelled) {
                        q.clear();
                        return;
                    }

                    if (done) {
                        Throwable ex = error;
                        if (ex != null) {
                            q.clear();

                            a.onError(ex);

                            worker.dispose();
                            return;
                        }
                        if (q.isEmpty()) {
                            a.onComplete();

                            worker.dispose();
                            return;
                        }
                    }
                }

                if (e != 0L && r != Long.MAX_VALUE) {
                    requested.addAndGet(-e);
                }

                int w = get();
                if (w == missed) {
                    consumed = c;
                    missed = addAndGet(-missed);
                    if (missed == 0) {
                        break;
                    }
                } else {
                    missed = w;
                }
            }
        }
