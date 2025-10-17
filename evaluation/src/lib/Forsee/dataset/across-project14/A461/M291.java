    @Override
    protected void subscribeActual(Observer<? super T> observer) {
        CancellableQueueFuseable<T> qs = new CancellableQueueFuseable<>();
        observer.onSubscribe(qs);

        if (!qs.isDisposed()) {

            try {
                run.run();
            } catch (Throwable ex) {
                Exceptions.throwIfFatal(ex);
                if (!qs.isDisposed()) {
                    observer.onError(ex);
                } else {
                    RxJavaPlugins.onError(ex);
                }
                return;
            }

            if (!qs.isDisposed()) {
                observer.onComplete();
            }
        }
    }
