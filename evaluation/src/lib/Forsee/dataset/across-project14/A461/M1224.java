        void drainLoop() {
            Subscriber<? super T> actual = this.downstream;
            int missed = 1;
            long e = emitted;
            int c = consumed;
            int lim = limit;
            for (;;) {

                long r = requested.get();

                while (e != r) {
                    if (cancelled) {
                        singleItem = null;
                        queue = null;
                        return;
                    }

                    if (errors.get() != null) {
                        singleItem = null;
                        queue = null;
                        errors.tryTerminateConsumer(downstream);
                        return;
                    }

                    int os = otherState;
                    if (os == OTHER_STATE_HAS_VALUE) {
                        T v = singleItem;
                        singleItem = null;
                        otherState = OTHER_STATE_CONSUMED_OR_EMPTY;
                        os = OTHER_STATE_CONSUMED_OR_EMPTY;
                        actual.onNext(v);

                        e++;
                        continue;
                    }

                    boolean d = mainDone;
                    SimplePlainQueue<T> q = queue;
                    T v = q != null ? q.poll() : null;
                    boolean empty = v == null;

                    if (d && empty && os == OTHER_STATE_CONSUMED_OR_EMPTY) {
                        queue = null;
                        actual.onComplete();
                        return;
                    }

                    if (empty) {
                        break;
                    }

                    actual.onNext(v);

                    e++;

                    if (++c == lim) {
                        c = 0;
                        mainSubscription.get().request(lim);
                    }
                }

                if (e == r) {
                    if (cancelled) {
                        singleItem = null;
                        queue = null;
                        return;
                    }

                    if (errors.get() != null) {
                        singleItem = null;
                        queue = null;
                        errors.tryTerminateConsumer(downstream);
                        return;
                    }

                    boolean d = mainDone;
                    SimplePlainQueue<T> q = queue;
                    boolean empty = q == null || q.isEmpty();

                    if (d && empty && otherState == 2) {
                        queue = null;
                        actual.onComplete();
                        return;
                    }
                }

                emitted = e;
                consumed = c;
                missed = addAndGet(-missed);
                if (missed == 0) {
                    break;
                }
            }
        }
