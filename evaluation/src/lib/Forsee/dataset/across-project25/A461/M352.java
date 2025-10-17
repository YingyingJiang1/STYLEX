        @Override
        public void onNext(T value) {
            if (upstream == DisposableHelper.DISPOSED) {
                return;
            }

            Iterator<? extends R> it;

            try {
                it = mapper.apply(value).iterator();
            } catch (Throwable ex) {
                Exceptions.throwIfFatal(ex);
                upstream.dispose();
                onError(ex);
                return;
            }

            Observer<? super R> a = downstream;

            for (;;) {
                boolean b;

                try {
                    b = it.hasNext();
                } catch (Throwable ex) {
                    Exceptions.throwIfFatal(ex);
                    upstream.dispose();
                    onError(ex);
                    return;
                }

                if (b) {
                    R v;

                    try {
                        v = Objects.requireNonNull(it.next(), "The iterator returned a null value");
                    } catch (Throwable ex) {
                        Exceptions.throwIfFatal(ex);
                        upstream.dispose();
                        onError(ex);
                        return;
                    }

                    a.onNext(v);
                } else {
                    break;
                }
            }
        }
