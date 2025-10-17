    @Override
    protected void subscribeActual(@NonNull Subscriber<? super T> s) {
        MulticastSubscription<T> ms = new MulticastSubscription<>(s, this);
        s.onSubscribe(ms);
        if (add(ms)) {
            if (ms.get() == Long.MIN_VALUE) {
                remove(ms);
            } else {
                drain();
            }
        } else {
            if (done) {
                Throwable ex = error;
                if (ex != null) {
                    s.onError(ex);
                    return;
                }
            }
            s.onComplete();
        }
    }
