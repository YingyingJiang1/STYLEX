        @Override
        public void onSuccess(T value) {
            Observer<? super R> a = downstream;
            Iterator<? extends R> iterator;
            boolean has;
            try {
                iterator = mapper.apply(value).iterator();

                has = iterator.hasNext();
            } catch (Throwable ex) {
                Exceptions.throwIfFatal(ex);
                downstream.onError(ex);
                return;
            }

            if (!has) {
                a.onComplete();
                return;
            }

            if (outputFused) {
                it = iterator;
                a.onNext(null);
                a.onComplete();
            } else {
                for (;;) {
                    if (cancelled) {
                        return;
                    }

                    R v;

                    try {
                        v = iterator.next();
                    } catch (Throwable ex) {
                        Exceptions.throwIfFatal(ex);
                        a.onError(ex);
                        return;
                    }

                    a.onNext(v);

                    if (cancelled) {
                        return;
                    }

                    boolean b;

                    try {
                        b = iterator.hasNext();
                    } catch (Throwable ex) {
                        Exceptions.throwIfFatal(ex);
                        a.onError(ex);
                        return;
                    }

                    if (!b) {
                        a.onComplete();
                        return;
                    }
                }
            }
        }
