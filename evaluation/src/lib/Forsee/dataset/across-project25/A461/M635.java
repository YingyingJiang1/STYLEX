    @Override
    protected void subscribeActual(MaybeObserver<? super T> observer) {
        D resource;

        try {
            resource = resourceSupplier.get();
        } catch (Throwable ex) {
            Exceptions.throwIfFatal(ex);
            EmptyDisposable.error(ex, observer);
            return;
        }

        MaybeSource<? extends T> source;

        try {
            source = Objects.requireNonNull(sourceSupplier.apply(resource), "The sourceSupplier returned a null MaybeSource");
        } catch (Throwable ex) {
            Exceptions.throwIfFatal(ex);
            if (eager) {
                try {
                    resourceDisposer.accept(resource);
                } catch (Throwable exc) {
                    Exceptions.throwIfFatal(exc);
                    EmptyDisposable.error(new CompositeException(ex, exc), observer);
                    return;
                }
            }

            EmptyDisposable.error(ex, observer);

            if (!eager) {
                try {
                    resourceDisposer.accept(resource);
                } catch (Throwable exc) {
                    Exceptions.throwIfFatal(exc);
                    RxJavaPlugins.onError(exc);
                }
            }
            return;
        }

        source.subscribe(new UsingObserver<T, D>(observer, resource, resourceDisposer, eager));
    }
