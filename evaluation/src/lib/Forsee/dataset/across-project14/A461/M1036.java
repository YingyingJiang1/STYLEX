        @Nullable
        @Override
        public R poll() throws Throwable {
            Iterator<? extends R> it = current;
            for (;;) {
                if (it == null) {
                    T v = queue.poll();
                    if (v == null) {
                        return null;
                    }

                    it = mapper.apply(v).iterator();

                    if (!it.hasNext()) {
                        it = null;
                        continue;
                    }
                    current = it;
                }

                R r = Objects.requireNonNull(it.next(), "The iterator returned a null value");

                if (!it.hasNext()) {
                    current = null;
                }

                return r;
            }
        }
