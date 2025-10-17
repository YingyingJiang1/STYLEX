    @Override
    protected void subscribeActual(Observer<? super R> observer) {
        ObservableSource<?>[] others = otherArray;
        int n = 0;
        if (others == null) {
            others = new ObservableSource[8];

            try {
                for (ObservableSource<?> p : otherIterable) {
                    if (n == others.length) {
                        others = Arrays.copyOf(others, n + (n >> 1));
                    }
                    others[n++] = p;
                }
            } catch (Throwable ex) {
                Exceptions.throwIfFatal(ex);
                EmptyDisposable.error(ex, observer);
                return;
            }

        } else {
            n = others.length;
        }

        if (n == 0) {
            new ObservableMap<>(source, new SingletonArrayFunc()).subscribeActual(observer);
            return;
        }

        WithLatestFromObserver<T, R> parent = new WithLatestFromObserver<>(observer, combiner, n);
        observer.onSubscribe(parent);
        parent.subscribe(others, n);

        source.subscribe(parent);
    }
