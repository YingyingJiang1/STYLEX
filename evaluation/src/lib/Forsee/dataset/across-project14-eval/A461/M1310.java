        void innerSuccess(InnerObserver inner, R value) {
            set.delete(inner);
            if (get() == 0 && compareAndSet(0, 1)) {
                boolean d = active.decrementAndGet() == 0;
                if (requested.get() != 0) {
                    downstream.onNext(value);

                    SpscLinkedArrayQueue<R> q = queue.get();

                    if (checkTerminate(d, q)) {
                        errors.tryTerminateConsumer(downstream);
                        return;
                    }
                    BackpressureHelper.produced(requested, 1);
                    if (maxConcurrency != Integer.MAX_VALUE) {
                        upstream.request(1);
                    }
                } else {
                    SpscLinkedArrayQueue<R> q = getOrCreateQueue();
                    synchronized (q) {
                        q.offer(value);
                    }
                }
                if (decrementAndGet() == 0) {
                    return;
                }
            } else {
                SpscLinkedArrayQueue<R> q = getOrCreateQueue();
                synchronized (q) {
                    q.offer(value);
                }
                active.decrementAndGet();
                if (getAndIncrement() != 0) {
                    return;
                }
            }
            drainLoop();
        }
