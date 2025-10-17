    static <T, R> boolean tryAsSingle(Object source,
            Function<? super T, ? extends SingleSource<? extends R>> mapper,
            Observer<? super R> observer) {
        if (source instanceof Supplier) {
            @SuppressWarnings("unchecked")
            Supplier<T> supplier = (Supplier<T>) source;
            SingleSource<? extends R> cs = null;
            try {
                T item = supplier.get();
                if (item != null) {
                    cs = Objects.requireNonNull(mapper.apply(item), "The mapper returned a null SingleSource");
                }
            } catch (Throwable ex) {
                Exceptions.throwIfFatal(ex);
                EmptyDisposable.error(ex, observer);
                return true;
            }

            if (cs == null) {
                EmptyDisposable.complete(observer);
            } else {
                cs.subscribe(SingleToObservable.create(observer));
            }
            return true;
        }
        return false;
    }
