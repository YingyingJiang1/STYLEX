        @SuppressWarnings("unchecked")
        void done() {
            if (decrementAndGet() == 0) {
                Object o1 = observer1.value;
                Object o2 = observer2.value;

                if (o1 != null && o2 != null) {
                    boolean b;

                    try {
                        b = isEqual.test((T)o1, (T)o2);
                    } catch (Throwable ex) {
                        Exceptions.throwIfFatal(ex);
                        downstream.onError(ex);
                        return;
                    }

                    downstream.onSuccess(b);
                } else {
                    downstream.onSuccess(o1 == null && o2 == null);
                }
            }
        }
