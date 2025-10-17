    @Override
    protected void subscribeActual(Observer<? super T> observer) {
        AsyncDisposable<T> as = new AsyncDisposable<>(observer, this);
        observer.onSubscribe(as);
        if (add(as)) {
            if (as.isDisposed()) {
                remove(as);
            }
        } else {
            Throwable ex = error;
            if (ex != null) {
                observer.onError(ex);
            } else {
                T v = value;
                if (v != null) {
                    as.complete(v);
                } else {
                    as.onComplete();
                }
            }
        }
    }
