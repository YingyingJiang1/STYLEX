        @Override
        public boolean hasNext() {
            for (;;) {
                if (isDisposed()) {
                    Throwable e = error;
                    if (e != null) {
                        throw ExceptionHelper.wrapOrThrow(e);
                    }
                    return false;
                }
                boolean d = done;
                boolean empty = queue.isEmpty();
                if (d) {
                    Throwable e = error;
                    if (e != null) {
                        throw ExceptionHelper.wrapOrThrow(e);
                    } else
                    if (empty) {
                        return false;
                    }
                }
                if (empty) {
                    BlockingHelper.verifyNonBlocking();
                    lock.lock();
                    try {
                        while (!done && queue.isEmpty() && !isDisposed()) {
                            condition.await();
                        }
                    } catch (InterruptedException ex) {
                        run();
                        throw ExceptionHelper.wrapOrThrow(ex);
                    } finally {
                        lock.unlock();
                    }
                } else {
                    return true;
                }
            }
        }
