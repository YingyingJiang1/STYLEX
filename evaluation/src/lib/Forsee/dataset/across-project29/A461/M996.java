        @Override
        public void onSubscribe(Subscription s) {
            if (SubscriptionHelper.setOnce(this.upstream, s)) {
                if (s instanceof QueueSubscription) {
                    @SuppressWarnings("unchecked")
                    QueueSubscription<T> qs = (QueueSubscription<T>) s;

                    int m = qs.requestFusion(QueueSubscription.ANY);
                    if (m == QueueSubscription.SYNC) {
                        sourceMode = m;
                        queue = qs;
                        done = true;
                        drain();
                        return;
                    }
                    if (m == QueueSubscription.ASYNC) {
                        sourceMode = m;
                        queue = qs;
                        QueueDrainHelper.request(s, prefetch);
                        return;
                    }
                }

                queue = QueueDrainHelper.createQueue(prefetch);

                QueueDrainHelper.request(s, prefetch);
            }
        }
