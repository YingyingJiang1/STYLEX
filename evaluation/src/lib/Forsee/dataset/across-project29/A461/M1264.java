        @Override
        public void onNext(T t) {
            long idx = index.get();
            if (idx == Long.MAX_VALUE || !index.compareAndSet(idx, idx + 1)) {
                return;
            }

            Disposable d = task.get();
            if (d != null) {
                d.dispose();
            }

            consumed++;

            downstream.onNext(t);

            Publisher<?> itemTimeoutPublisher;

            try {
                itemTimeoutPublisher = Objects.requireNonNull(
                        itemTimeoutIndicator.apply(t),
                        "The itemTimeoutIndicator returned a null Publisher.");
            } catch (Throwable ex) {
                Exceptions.throwIfFatal(ex);
                upstream.get().cancel();
                index.getAndSet(Long.MAX_VALUE);
                downstream.onError(ex);
                return;
            }

            TimeoutConsumer consumer = new TimeoutConsumer(idx + 1, this);
            if (task.replace(consumer)) {
                itemTimeoutPublisher.subscribe(consumer);
            }
        }
