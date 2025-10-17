        @Override
        public void onNext(T t) {
            if (done) {
                return;
            }

            ArrayDeque<C> bs = buffers;

            int i = index;

            if (i++ == 0) {
                C b;

                try {
                    b = Objects.requireNonNull(bufferSupplier.get(), "The bufferSupplier returned a null buffer");
                } catch (Throwable e) {
                    Exceptions.throwIfFatal(e);
                    cancel();
                    onError(e);
                    return;
                }

                bs.offer(b);
            }

            C b = bs.peek();

            if (b.size() + 1 == size) {
                bs.poll();

                b.add(t);

                produced++;

                downstream.onNext(b);
            }

            for (C b0 : bs) {
                b0.add(t);
            }

            if (i == skip) {
                i = 0;
            }
            index = i;
        }
