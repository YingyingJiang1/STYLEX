    @Override
    protected void subscribeActual(final SingleObserver<? super T> observer) {

        final U resource; // NOPMD

        try {
            resource = resourceSupplier.get();
        } catch (Throwable ex) {
            Exceptions.throwIfFatal(ex);
            EmptyDisposable.error(ex, observer);
            return;
        }

        SingleSource<? extends T> source;

        try {
            source = Objects.requireNonNull(singleFunction.apply(resource), "The singleFunction returned a null SingleSource");
        } catch (Throwable ex) {
            Exceptions.throwIfFatal(ex);

            if (eager) {
                try {
                    disposer.accept(resource);
                } catch (Throwable exc) {
                    Exceptions.throwIfFatal(exc);
                    ex = new CompositeException(ex, exc);
                }
            }
            EmptyDisposable.error(ex, observer);
            if (!eager) {
                try {
                    disposer.accept(resource);
                } catch (Throwable exc) {
                    Exceptions.throwIfFatal(exc);
                    RxJavaPlugins.onError(exc);
                }
            }
            return;
        }

        source.subscribe(new UsingSingleObserver<T, U>(observer, resource, eager, disposer));
    }
