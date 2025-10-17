    public static <T> void postComplete(Subscriber<? super T> actual,
                                        Queue<T> queue,
                                        AtomicLong state,
                                        BooleanSupplier isCancelled) {

        if (queue.isEmpty()) {
            actual.onComplete();
            return;
        }

        if (postCompleteDrain(state.get(), actual, queue, state, isCancelled)) {
            return;
        }

        for (; ; ) {
            long r = state.get();

            if ((r & COMPLETED_MASK) != 0L) {
                return;
            }

            long u = r | COMPLETED_MASK;
            // (active, r) -> (complete, r) transition
            if (state.compareAndSet(r, u)) {
                // if the requested amount was non-zero, drain the queue
                if (r != 0L) {
                    postCompleteDrain(u, actual, queue, state, isCancelled);
                }

                return;
            }
        }

    }
