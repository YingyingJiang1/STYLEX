    static <T> boolean postCompleteDrain(long n,
                                         Subscriber<? super T> actual,
                                         Queue<T> queue,
                                         AtomicLong state,
                                         BooleanSupplier isCancelled) {

// TODO enable fast-path
//        if (n == -1 || n == Long.MAX_VALUE) {
//            for (;;) {
//                if (isCancelled.getAsBoolean()) {
//                    break;
//                }
//
//                T v = queue.poll();
//
//                if (v == null) {
//                    actual.onComplete();
//                    break;
//                }
//
//                actual.onNext(v);
//            }
//
//            return true;
//        }

        long e = n & COMPLETED_MASK;

        for (; ; ) {

            while (e != n) {
                if (isCancelled(isCancelled)) {
                    return true;
                }

                T t = queue.poll();

                if (t == null) {
                    actual.onComplete();
                    return true;
                }

                actual.onNext(t);
                e++;
            }

            if (isCancelled(isCancelled)) {
                return true;
            }

            if (queue.isEmpty()) {
                actual.onComplete();
                return true;
            }

            n = state.get();

            if (n == e) {

                n = state.addAndGet(-(e & REQUESTED_MASK));

                if ((n & REQUESTED_MASK) == 0L) {
                    return false;
                }

                e = n & COMPLETED_MASK;
            }
        }

    }
