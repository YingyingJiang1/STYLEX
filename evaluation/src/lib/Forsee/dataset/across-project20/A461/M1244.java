        @Override
        void fastPath() {
            T[] arr = array;
            int f = arr.length;
            Subscriber<? super T> a = downstream;

            for (int i = index; i != f; i++) {
                if (cancelled) {
                    return;
                }
                T t = arr[i];
                if (t == null) {
                    a.onError(new NullPointerException("The element at index " + i + " is null"));
                    return;
                } else {
                    a.onNext(t);
                }
            }
            if (cancelled) {
                return;
            }
            a.onComplete();
        }
