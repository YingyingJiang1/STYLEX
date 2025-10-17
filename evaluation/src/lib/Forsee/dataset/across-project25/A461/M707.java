        @Nullable
        @Override
        public T poll() {
            int ci = consumerIndex;
            if (ci == length()) {
                return null;
            }
            AtomicInteger pi = producerIndex;
            for (;;) {
                T v = get(ci);
                if (v != null) {
                    consumerIndex = ci + 1;
                    lazySet(ci, null);
                    return v;
                }
                if (pi.get() == ci) {
                    return null;
                }
            }
        }
