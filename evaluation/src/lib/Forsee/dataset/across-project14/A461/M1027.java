        @Override
        public void onSubscribe(Subscription s) {
            if (SubscriptionHelper.validate(this.upstream, s)) {
                this.upstream = s;

                if (s instanceof QueueSubscription) {
                    @SuppressWarnings("unchecked")
                    QueueSubscription<T> qs = (QueueSubscription<T>) s;

                    int m = qs.requestFusion(ANY);

                    if (m == SYNC) {
                        fusionMode = m;
                        this.queue = qs;
                        done = true;

                        downstream.onSubscribe(this);

                        return;
                    }
                    if (m == ASYNC) {
                        fusionMode = m;
                        this.queue = qs;

                        downstream.onSubscribe(this);

                        s.request(prefetch);
                        return;
                    }
                }

                queue = new SpscArrayQueue<>(prefetch);

                downstream.onSubscribe(this);

                s.request(prefetch);
            }
        }
