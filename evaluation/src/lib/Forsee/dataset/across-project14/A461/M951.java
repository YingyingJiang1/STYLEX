    @Override
    protected void subscribeActual(CompletableObserver observer) {
        InnerCompletableCache inner = new InnerCompletableCache(observer);
        observer.onSubscribe(inner);

        if (add(inner)) {
            if (inner.isDisposed()) {
                remove(inner);
            }

            if (once.compareAndSet(false, true)) {
                source.subscribe(this);
            }
        } else {
            Throwable ex = error;
            if (ex != null) {
                observer.onError(ex);
            } else {
                observer.onComplete();
            }
        }
    }
