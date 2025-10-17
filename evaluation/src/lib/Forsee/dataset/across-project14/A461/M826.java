    @Override
    public void subscribe(Subscriber<? super C>[] subscribers) {
        subscribers = RxJavaPlugins.onSubscribe(this, subscribers);

        if (!validate(subscribers)) {
            return;
        }

        int n = subscribers.length;
        @SuppressWarnings("unchecked")
        Subscriber<T>[] parents = new Subscriber[n];

        for (int i = 0; i < n; i++) {

            C initialValue;

            try {
                initialValue = Objects.requireNonNull(initialCollection.get(), "The initialSupplier returned a null value");
            } catch (Throwable ex) {
                Exceptions.throwIfFatal(ex);
                reportError(subscribers, ex);
                return;
            }

            parents[i] = new ParallelCollectSubscriber<>(subscribers[i], initialValue, collector);
        }

        source.subscribe(parents);
    }
