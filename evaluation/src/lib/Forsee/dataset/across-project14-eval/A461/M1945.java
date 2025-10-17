    @Override
    protected void subscribeActual(Observer<? super R> observer) {
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
                EmptyDisposable.error(ex, observer);
                return;
            }

            if (stream != null) {
                ObservableFromStream.subscribeStream(observer, stream);
            } else {
                EmptyDisposable.complete(observer);
            }
        } else {
            source.subscribe(new FlatMapStreamObserver<>(observer, mapper));
        }
    }
