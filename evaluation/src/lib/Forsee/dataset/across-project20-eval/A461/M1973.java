        public void run() {
            if (outputFused) {
                return;
            }
            Iterator<T> iterator = this.iterator;
            Observer<? super T> downstream = this.downstream;

            for (;;) {
                if (disposed) {
                    clear();
                    break;
                }

                T next;
                try {
                    next = Objects.requireNonNull(iterator.next(), "The Stream's Iterator.next returned a null value");
                } catch (Throwable ex) {
                    Exceptions.throwIfFatal(ex);
                    downstream.onError(ex);
                    disposed = true;
                    continue;
                }

                if (disposed) {
                    continue;
                }

                downstream.onNext(next);

                if (disposed) {
                    continue;
                }

                try {
                    if (iterator.hasNext()) {
                        continue;
                    }
                } catch (Throwable ex) {
                    Exceptions.throwIfFatal(ex);
                    downstream.onError(ex);
                    disposed = true;
                    continue;
                }

                downstream.onComplete();
                disposed = true;
            }
        }
