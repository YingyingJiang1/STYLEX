        void innerNext(int index, T item) {
            boolean shouldDrain = false;
            synchronized (this) {
                Object[] latest = this.latest;
                if (latest == null) {
                    return;
                }
                Object o = latest[index];
                int a = active;
                if (o == null) {
                    active = ++a;
                }
                latest[index] = item;
                if (a == latest.length) {
                    queue.offer(latest.clone());
                    shouldDrain = true;
                }
            }
            if (shouldDrain) {
                drain();
            }
        }
