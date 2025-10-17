    public void blockingConsume(CompletableObserver observer) {
        if (getCount() != 0) {
            try {
                BlockingHelper.verifyNonBlocking();
                await();
            } catch (InterruptedException ex) {
                dispose();
                observer.onError(ex);
                return;
            }
        }
        if (isDisposed()) {
            return;
        }

        Throwable ex = error;
        if (ex != null) {
            observer.onError(ex);
        } else {
            observer.onComplete();
        }
    }
