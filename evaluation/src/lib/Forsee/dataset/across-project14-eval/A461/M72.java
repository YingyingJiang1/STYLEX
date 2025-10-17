    @Override
    public final void onSubscribe(Subscription s) {
        if (SubscriptionHelper.validate(upstream, s)) {
            upstream = s;
            if (s instanceof QueueSubscription) {
                @SuppressWarnings("unchecked")
                QueueSubscription<T> qs = (QueueSubscription<T>)s;
                int mode = qs.requestFusion(QueueFuseable.ANY | QueueFuseable.BOUNDARY);
                if (mode == QueueFuseable.SYNC) {
                    queue = qs;
                    syncFused = true;
                    done = true;

                    onSubscribeDownstream();

                    drain();
                    return;
                }
                else if (mode == QueueFuseable.ASYNC) {
                    queue = qs;

                    onSubscribeDownstream();

                    upstream.request(prefetch);
                    return;
                }
            }

            queue = new SpscArrayQueue<>(prefetch);
            onSubscribeDownstream();
            upstream.request(prefetch);
        }
    }
