        @Override
        public void subscribe(Observer<? super T> observer) {
            for (;;) {
                int s = once.get();
                if ((s & HAS_SUBSCRIBER) != 0) {
                    break;
                }
                int u = s | HAS_SUBSCRIBER;
                if (once.compareAndSet(s, u)) {
                    observer.onSubscribe(this);
                    actual.lazySet(observer);
                    if (cancelled.get()) {
                        actual.lazySet(null);
                    } else {
                        drain();
                    }
                    return;
                }
            }
            EmptyDisposable.error(new IllegalStateException("Only one Observer allowed!"), observer);
        }
