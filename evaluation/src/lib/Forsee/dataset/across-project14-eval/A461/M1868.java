        @Override
        public void onNext(T t) {
            if (done) {
                return;
            }

            if (sourceMode != NONE) {
                downstream.onNext(null);
                return;
            }

            Optional<? extends R> result;
            try {
                result = Objects.requireNonNull(mapper.apply(t), "The mapper returned a null Optional");
            } catch (Throwable ex) {
                fail(ex);
                return;
            }

            if (result.isPresent()) {
                downstream.onNext(result.get());
            }
        }
