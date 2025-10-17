        @Override
        public boolean tryOnNext(T t) {
            if (done) {
                return false;
            }
            if (sourceMode != NONE) {
                return downstream.tryOnNext(t);
            }

            K key;

            try {
                key = keySelector.apply(t);
                if (hasValue) {
                    boolean equal = comparer.test(last, key);
                    last = key;
                    if (equal) {
                        return false;
                    }
                } else {
                    hasValue = true;
                    last = key;
                }
            } catch (Throwable ex) {
               fail(ex);
               return true;
            }

            downstream.onNext(t);
            return true;
        }
