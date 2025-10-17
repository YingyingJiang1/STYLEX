        @SuppressWarnings("unchecked")
        @Override
        public void subscribeActual(Observer<? super R> observer) {
            ObservableSource<? extends R> other;
            try {
                other = Objects.requireNonNull(mapper.apply(value), "The mapper returned a null ObservableSource");
            } catch (Throwable e) {
                Exceptions.throwIfFatal(e);
                EmptyDisposable.error(e, observer);
                return;
            }
            if (other instanceof Supplier) {
                R u;

                try {
                    u = ((Supplier<R>)other).get();
                } catch (Throwable ex) {
                    Exceptions.throwIfFatal(ex);
                    EmptyDisposable.error(ex, observer);
                    return;
                }

                if (u == null) {
                    EmptyDisposable.complete(observer);
                    return;
                }
                ScalarDisposable<R> sd = new ScalarDisposable<>(observer, u);
                observer.onSubscribe(sd);
                sd.run();
            } else {
                other.subscribe(observer);
            }
        }
