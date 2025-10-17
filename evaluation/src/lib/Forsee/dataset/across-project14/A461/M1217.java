        @Override
        public void onNext(T t) {
            if (compareAndSet(0, 1)) {
                long e = emitted;
                if (requested.get() != e) {
                    SimplePlainQueue<T> q = queue;
                    if (q == null || q.isEmpty()) {

                        emitted = e + 1;
                        downstream.onNext(t);

                        int c = consumed + 1;
                        if (c == limit) {
                            consumed = 0;
                            mainSubscription.get().request(c);
                        } else {
                            consumed = c;
                        }
                    } else {
                        q.offer(t);
                    }
                } else {
                    SimplePlainQueue<T> q = getOrCreateQueue();
                    q.offer(t);
                }
                if (decrementAndGet() == 0) {
                    return;
                }
            } else {
                SimplePlainQueue<T> q = getOrCreateQueue();
                q.offer(t);
                if (getAndIncrement() != 0) {
                    return;
                }
            }
            drainLoop();
        }
