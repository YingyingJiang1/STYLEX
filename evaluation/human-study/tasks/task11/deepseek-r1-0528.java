
void drainLoop() {
    int missed = 1;

    final JoinInnerSubscriber<T>[] s = this.subscribers;
    final int n = s.length;
    final Subscriber<? super T> a = this.downstream;

    for (;;) {

        final long r = requested.get();
        long e = 0;

        middle:
        while (e != r) {
            if (cancelled) {
                cleanup();
                return;
            }

            final Throwable ex = errors.get();
            if (ex != null) {
                cleanup();
                a.onError(ex);
                return;
            }

            final boolean d = done.get() == 0;

            boolean empty = true;

            for (int i = 0; i < s.length; i++) {
                final JoinInnerSubscriber<T> inner = s[i];
                final SimplePlainQueue<T> q = inner.queue;
                if (q != null) {
                    final T v = q.poll();

                    if (v != null) {
                        empty = false;
                        a.onNext(v);
                        inner.requestOne();
                        if (++e == r) {
                            break middle;
                        }
                    }
                }
            }

            if (d && empty) {
                a.onComplete();
                return;
            }

            if (empty) {
                break;
            }
        }

        if (e == r) {
            if (cancelled) {
                cleanup();
                return;
            }

            final Throwable ex = errors.get();
            if (ex != null) {
                cleanup();
                a.onError(ex);
                return;
            }

            final boolean d = done.get() == 0;

            boolean empty = true;

            for (int i = 0; i < n; i++) {
                final JoinInnerSubscriber<T> inner = s[i];

                final SimpleQueue<T> q = inner.queue;
                if (q != null && !q.isEmpty()) {
                    empty = false;
                    break;
                }
            }

            if (d && empty) {
                a.onComplete();
                return;
            }
        }

        if (e != 0) {
            BackpressureHelper.produced(requested, e);
        }

        missed = addAndGet(-missed);
        if (missed == 0) {
            break;
        }
    }
}
