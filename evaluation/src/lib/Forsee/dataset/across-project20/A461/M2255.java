    @Override
    public void onSubscribe(@NonNull Subscription s) {
        if (SubscriptionHelper.setOnce(upstream, s)) {
            if (s instanceof QueueSubscription) {
                @SuppressWarnings("unchecked")
                QueueSubscription<T> qs = (QueueSubscription<T>)s;

                int m = qs.requestFusion(QueueSubscription.ANY);
                if (m == QueueSubscription.SYNC) {
                    fusionMode = m;
                    queue = qs;
                    done = true;
                    drain();
                    return;
                }
                if (m == QueueSubscription.ASYNC) {
                    fusionMode = m;
                    queue = qs;

                    s.request(bufferSize);
                    return;
                }
            }

            queue = new SpscArrayQueue<>(bufferSize);

            s.request(bufferSize);
        }
    }
