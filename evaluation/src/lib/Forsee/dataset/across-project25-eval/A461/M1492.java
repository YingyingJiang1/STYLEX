        @Override
        public boolean tryOnNext(T t) {
            if (done) {
                return false;
            }
            AtomicReferenceArray<Object> ara = values;
            int n = ara.length();
            Object[] objects = new Object[n + 1];
            objects[0] = t;

            for (int i = 0; i < n; i++) {
                Object o = ara.get(i);
                if (o == null) {
                    // somebody hasn't signalled yet, skip this T
                    return false;
                }
                objects[i + 1] = o;
            }

            R v;

            try {
                v = Objects.requireNonNull(combiner.apply(objects), "The combiner returned a null value");
            } catch (Throwable ex) {
                Exceptions.throwIfFatal(ex);
                cancel();
                onError(ex);
                return false;
            }

            HalfSerializer.onNext(downstream, v, this, error);
            return true;
        }
