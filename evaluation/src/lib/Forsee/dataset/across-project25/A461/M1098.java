    @SuppressWarnings("unchecked")
    void remove(CacheSubscription<T> consumer) {
        for (;;) {
            CacheSubscription<T>[] current = subscribers.get();
            int n = current.length;
            if (n == 0) {
                return;
            }

            int j = -1;
            for (int i = 0; i < n; i++) {
                if (current[i] == consumer) {
                    j = i;
                    break;
                }
            }

            if (j < 0) {
                return;
            }
            CacheSubscription<T>[] next;

            if (n == 1) {
                next = EMPTY;
            } else {
                next = new CacheSubscription[n - 1];
                System.arraycopy(current, 0, next, 0, j);
                System.arraycopy(current, j + 1, next, j, n - j - 1);
            }

            if (subscribers.compareAndSet(current, next)) {
                return;
            }
        }
    }
