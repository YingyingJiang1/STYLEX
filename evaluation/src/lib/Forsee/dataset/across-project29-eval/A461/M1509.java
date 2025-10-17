        @Override
        void runAsync() {
            int missed = 1;

            final Subscriber<? super T> a = downstream;
            final SimpleQueue<T> q = queue;

            long e = produced;

            for (;;) {

                long r = requested.get();

                while (e != r) {
                    boolean d = done;
                    T v;

                    try {
                        v = q.poll();
                    } catch (Throwable ex) {
                        Exceptions.throwIfFatal(ex);

                        cancelled = true;
                        upstream.cancel();
                        q.clear();

                        a.onError(ex);
                        worker.dispose();
                        return;
                    }

                    boolean empty = v == null;

                    if (checkTerminated(d, empty, a)) {
                        return;
                    }

                    if (empty) {
                        break;
                    }

                    a.onNext(v);

                    e++;
                    if (e == limit) {
                        if (r != Long.MAX_VALUE) {
                            r = requested.addAndGet(-e);
                        }
                        upstream.request(e);
                        e = 0L;
                    }
                }

                if (e == r && checkTerminated(done, q.isEmpty(), a)) {
                    return;
                }

                int w = get();
                if (missed == w) {
                    produced = e;
                    missed = addAndGet(-missed);
                    if (missed == 0) {
                        break;
                    }
                } else {
                    missed = w;
                }
            }
        }
