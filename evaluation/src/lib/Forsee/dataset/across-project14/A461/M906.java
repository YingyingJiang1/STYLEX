        @Override
        public void onSubscribe(Subscription s) {
            if (SubscriptionHelper.validate(this.upstream, s)) {
                this.upstream = s;

                if (s instanceof QueueSubscription) {
                    @SuppressWarnings("unchecked")
                    QueueSubscription<T> qs = (QueueSubscription<T>) s;

                    int m = qs.requestFusion(QueueSubscription.ANY | QueueSubscription.BOUNDARY);

                    if (m == QueueSubscription.SYNC) {
                        sourceMode = m;
                        queue = qs;
                        done = true;
                        setupSubscribers();
                        drain();
                        return;
                    } else
                    if (m == QueueSubscription.ASYNC) {
                        sourceMode = m;
                        queue = qs;

                        setupSubscribers();

                        s.request(prefetch);

                        return;
                    }
                }

                queue = new SpscArrayQueue<>(prefetch);

                setupSubscribers();

                s.request(prefetch);
            }
        }
