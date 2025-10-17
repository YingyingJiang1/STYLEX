        @Override
        public void onSubscribe(Disposable d) {
            if (DisposableHelper.setOnce(this, d)) {
                if (d instanceof QueueDisposable) {
                    @SuppressWarnings("unchecked")
                    QueueDisposable<R> qd = (QueueDisposable<R>) d;

                    int m = qd.requestFusion(QueueDisposable.ANY | QueueDisposable.BOUNDARY);
                    if (m == QueueDisposable.SYNC) {
                        queue = qd;
                        done = true;
                        parent.drain();
                        return;
                    }
                    if (m == QueueDisposable.ASYNC) {
                        queue = qd;
                        return;
                    }
                }

                queue = new SpscLinkedArrayQueue<>(bufferSize);
            }
        }
