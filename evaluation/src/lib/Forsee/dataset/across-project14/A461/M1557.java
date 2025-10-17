    @Override
    protected void subscribeActual(final SingleObserver<? super T> observer) {
        CacheDisposable<T> d = new CacheDisposable<>(observer, this);
        observer.onSubscribe(d);

        if (add(d)) {
            if (d.isDisposed()) {
                remove(d);
            }
        } else {
            Throwable ex = error;
            if (ex != null) {
                observer.onError(ex);
            } else {
                observer.onSuccess(value);
            }
            return;
        }

        if (wip.getAndIncrement() == 0) {
            source.subscribe(this);
        }
    }
