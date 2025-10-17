        @Override
        @SuppressWarnings({ "unchecked", "rawtypes" })
        public void onNext(T t) {
            SwitchMapMaybeObserver<R> current = inner.get();
            if (current != null) {
                current.dispose();
            }

            MaybeSource<? extends R> ms;

            try {
                ms = Objects.requireNonNull(mapper.apply(t), "The mapper returned a null MaybeSource");
            } catch (Throwable ex) {
                Exceptions.throwIfFatal(ex);
                upstream.dispose();
                inner.getAndSet((SwitchMapMaybeObserver)INNER_DISPOSED);
                onError(ex);
                return;
            }

            SwitchMapMaybeObserver<R> observer = new SwitchMapMaybeObserver<>(this);

            for (;;) {
                current = inner.get();
                if (current == INNER_DISPOSED) {
                    break;
                }
                if (inner.compareAndSet(current, observer)) {
                    ms.subscribe(observer);
                    break;
                }
            }
        }
