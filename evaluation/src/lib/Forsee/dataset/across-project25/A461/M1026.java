    @SuppressWarnings("unchecked")
    @Override
    public void subscribeActual(Subscriber<? super R> s) {
        if (source instanceof Supplier) {
            T v;

            try {
                v = ((Supplier<T>)source).get();
            } catch (Throwable ex) {
                Exceptions.throwIfFatal(ex);
                EmptySubscription.error(ex, s);
                return;
            }

            if (v == null) {
                EmptySubscription.complete(s);
                return;
            }

            Iterator<? extends R> it;

            try {
                Iterable<? extends R> iterable = mapper.apply(v);

                it = iterable.iterator();
            } catch (Throwable ex) {
                Exceptions.throwIfFatal(ex);
                EmptySubscription.error(ex, s);
                return;
            }

            FlowableFromIterable.subscribe(s, it);

            return;
        }
        source.subscribe(new FlattenIterableSubscriber<>(s, mapper, prefetch));
    }
