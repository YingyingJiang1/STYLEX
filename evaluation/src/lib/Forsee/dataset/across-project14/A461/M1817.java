        @Override
        public void onSuccess(@NonNull T t) {
            try {
                Stream<? extends R> stream = Objects.requireNonNull(mapper.apply(t), "The mapper returned a null Stream");
                Iterator<? extends R> iterator = stream.iterator();
                AutoCloseable c = stream;

                if (!iterator.hasNext()) {
                    downstream.onComplete();
                    close(c);
                    return;
                }
                this.iterator = iterator;
                this.close = stream;
            } catch (Throwable ex) {
                Exceptions.throwIfFatal(ex);
                downstream.onError(ex);
                return;
            }
            drain();
        }
