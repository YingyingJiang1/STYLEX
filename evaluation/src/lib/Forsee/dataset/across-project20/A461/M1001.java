        @SuppressWarnings("unchecked")
        void remove(MulticastSubscription<T> s) {
            for (;;) {
                MulticastSubscription<T>[] current = subscribers.get();
                int n = current.length;
                if (n == 0) {
                    return;
                }
                int j = -1;

                for (int i = 0; i < n; i++) {
                    if (current[i] == s) {
                        j = i;
                        break;
                    }
                }

                if (j < 0) {
                    return;
                }
                MulticastSubscription<T>[] next;
                if (n == 1) {
                    next = EMPTY;
                } else {
                    next = new MulticastSubscription[n - 1];
                    System.arraycopy(current, 0, next, 0, j);
                    System.arraycopy(current, j + 1, next, j, n - j - 1);
                }
                if (subscribers.compareAndSet(current, next)) {
                    return;
                }
            }
        }
