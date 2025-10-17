        @Override
        public void subscribe(Subscriber<? super T> subscriber) {
            for (;;) {
                int s = once.get();
                if ((s & HAS_SUBSCRIBER) != 0) {
                    break;
                }
                int u = s | HAS_SUBSCRIBER;
                if (once.compareAndSet(s, u)) {
                    subscriber.onSubscribe(this);
                    actual.lazySet(subscriber);
                    if (cancelled.get()) {
                        actual.lazySet(null);
                    } else {
                        drain();
                    }
                    return;
                }
            }
            EmptySubscription.error(new IllegalStateException("Only one Subscriber allowed!"), subscriber);
        }
