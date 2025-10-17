    @Override
    protected void subscribeActual(Subscriber<? super R> s) {
        if (source instanceof Supplier) {
            Stream<? extends R> stream = null;
            try {
                @SuppressWarnings("unchecked")
                T t = ((Supplier<T>)source).get();
                if (t != null) {
                    stream = Objects.requireNonNull(mapper.apply(t), "The mapper returned a null Stream");
                }
            } catch (Throwable ex) {
                Exceptions.throwIfFatal(ex);
                EmptySubscription.error(ex, s);
                return;
            }

            if (stream != null) {
                FlowableFromStream.subscribeStream(s, stream);
            } else {
                EmptySubscription.complete(s);
            }
        } else {
            source.subscribe(subscribe(s, mapper, prefetch));
        }
    }
