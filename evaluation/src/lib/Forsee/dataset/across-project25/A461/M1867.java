        void drain() {
            if (getAndIncrement() != 0) {
                return;
            }

            int missed = 1;
            Observer<? super R> downstream = this.downstream;
            Iterator<? extends R> it = iterator;

            for (;;) {

                if (disposed) {
                    clear();
                } else {
                    if (outputFused) {
                        downstream.onNext(null);
                        downstream.onComplete();
                    } else {
                        R item;
                        try {
                            item = it.next();
                        } catch (Throwable ex) {
                            Exceptions.throwIfFatal(ex);
                            downstream.onError(ex);
                            disposed = true;
                            continue;
                        }

                        if (disposed) {
                            continue;
                        }

                        downstream.onNext(item);

                        if (disposed) {
                            continue;
                        }

                        boolean has;
                        try {
                            has = it.hasNext();
                        } catch (Throwable ex) {
                            Exceptions.throwIfFatal(ex);
                            downstream.onError(ex);
                            disposed = true;
                            continue;
                        }

                        if (disposed) {
                            continue;
                        }

                        if (!has) {
                            downstream.onComplete();
                            disposed = true;
                        }
                        continue;
                    }
                }

                missed = addAndGet(-missed);
                if (missed == 0) {
                    return;
                }
            }
        }
