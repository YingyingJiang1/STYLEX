    @Override
    protected void subscribeActual(Subscriber<? super R> s) {
        Publisher<?>[] others = otherArray;
        int n = 0;
        if (others == null) {
            others = new Publisher[8];

            try {
                for (Publisher<?> p : otherIterable) {
                    if (n == others.length) {
                        others = Arrays.copyOf(others, n + (n >> 1));
                    }
                    others[n++] = p;
                }
            } catch (Throwable ex) {
                Exceptions.throwIfFatal(ex);
                EmptySubscription.error(ex, s);
                return;
            }

        } else {
            n = others.length;
        }

        if (n == 0) {
            new FlowableMap<>(source, new SingletonArrayFunc()).subscribeActual(s);
            return;
        }

        WithLatestFromSubscriber<T, R> parent = new WithLatestFromSubscriber<>(s, combiner, n);
        s.onSubscribe(parent);
        parent.subscribe(others, n);

        source.subscribe(parent);
    }
