        @Override
        public void run() {
            for (;;) {
                if (disposed) {
                    queue.clear();
                    return;
                }
                if (!active) {

                    boolean d = done;

                    T t;

                    try {
                        t = queue.poll();
                    } catch (Throwable ex) {
                        Exceptions.throwIfFatal(ex);
                        dispose();
                        queue.clear();
                        downstream.onError(ex);
                        worker.dispose();
                        return;
                    }

                    boolean empty = t == null;

                    if (d && empty) {
                        disposed = true;
                        downstream.onComplete();
                        worker.dispose();
                        return;
                    }

                    if (!empty) {
                        ObservableSource<? extends U> o;

                        try {
                            o = Objects.requireNonNull(mapper.apply(t), "The mapper returned a null ObservableSource");
                        } catch (Throwable ex) {
                            Exceptions.throwIfFatal(ex);
                            dispose();
                            queue.clear();
                            downstream.onError(ex);
                            worker.dispose();
                            return;
                        }

                        active = true;
                        o.subscribe(inner);
                    }
                }

                if (decrementAndGet() == 0) {
                    break;
                }
            }
        }
