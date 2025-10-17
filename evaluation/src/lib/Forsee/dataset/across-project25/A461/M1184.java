        @Override
        void slowPath(long r) {
            long e = 0L;
            Iterator<? extends T> it = this.iterator;
            ConditionalSubscriber<? super T> a = downstream;

            for (;;) {

                while (e != r) {

                    if (cancelled) {
                        return;
                    }

                    T t;

                    try {
                        t = it.next();
                    } catch (Throwable ex) {
                        Exceptions.throwIfFatal(ex);
                        a.onError(ex);
                        return;
                    }

                    if (cancelled) {
                        return;
                    }

                    boolean b;
                    if (t == null) {
                        a.onError(new NullPointerException("Iterator.next() returned a null value"));
                        return;
                    } else {
                        b = a.tryOnNext(t);
                    }

                    if (cancelled) {
                        return;
                    }

                    boolean hasNext;

                    try {
                        hasNext = it.hasNext();
                    } catch (Throwable ex) {
                        Exceptions.throwIfFatal(ex);
                        a.onError(ex);
                        return;
                    }

                    if (!hasNext) {
                        if (!cancelled) {
                            a.onComplete();
                        }
                        return;
                    }

                    if (b) {
                        e++;
                    }
                }

                r = get();
                if (e == r) {
                    r = addAndGet(-e);
                    if (r == 0L) {
                        return;
                    }
                    e = 0L;
                }
            }
        }
