        @Override
        void fastPath() {
            Iterator<? extends T> it = this.iterator;
            ConditionalSubscriber<? super T> a = downstream;
            for (;;) {
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

                if (t == null) {
                    a.onError(new NullPointerException("Iterator.next() returned a null value"));
                    return;
                } else {
                    a.tryOnNext(t);
                }

                if (cancelled) {
                    return;
                }

                boolean b;

                try {
                    b = it.hasNext();
                } catch (Throwable ex) {
                    Exceptions.throwIfFatal(ex);
                    a.onError(ex);
                    return;
                }

                if (!b) {
                    if (!cancelled) {
                        a.onComplete();
                    }
                    return;
                }
            }
        }
