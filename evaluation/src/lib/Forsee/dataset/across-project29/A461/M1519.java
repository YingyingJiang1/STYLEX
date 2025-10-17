        void drainLoop() {
            BaseEmitter<T> e = emitter;
            SimplePlainQueue<T> q = queue;
            AtomicThrowable errors = this.errors;
            int missed = 1;
            for (;;) {

                for (;;) {
                    if (e.isCancelled()) {
                        q.clear();
                        return;
                    }

                    if (errors.get() != null) {
                        q.clear();
                        errors.tryTerminateConsumer(e);
                        return;
                    }

                    boolean d = done;

                    T v = q.poll();

                    boolean empty = v == null;

                    if (d && empty) {
                        e.onComplete();
                        return;
                    }

                    if (empty) {
                        break;
                    }

                    e.onNext(v);
                }

                missed = addAndGet(-missed);
                if (missed == 0) {
                    break;
                }
            }
        }
