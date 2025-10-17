    final void drainLoop() {
        int missed = 1;

        long requestAmount = 0L;
        Subscription requestTarget = null;

        for (; ; ) {

            Subscription ms = missedSubscription.get();

            if (ms != null) {
                ms = missedSubscription.getAndSet(null);
            }

            long mr = missedRequested.get();
            if (mr != 0L) {
                mr = missedRequested.getAndSet(0L);
            }

            long mp = missedProduced.get();
            if (mp != 0L) {
                mp = missedProduced.getAndSet(0L);
            }

            Subscription a = actual;

            if (cancelled) {
                if (a != null) {
                    a.cancel();
                    actual = null;
                }
                if (ms != null) {
                    ms.cancel();
                }
            } else {
                long r = requested;
                if (r != Long.MAX_VALUE) {
                    long u = BackpressureHelper.addCap(r, mr);

                    if (u != Long.MAX_VALUE) {
                        long v = u - mp;
                        if (v < 0L) {
                            SubscriptionHelper.reportMoreProduced(v);
                            v = 0;
                        }
                        r = v;
                    } else {
                        r = u;
                    }
                    requested = r;
                }

                if (ms != null) {
                    if (a != null && cancelOnReplace) {
                        a.cancel();
                    }
                    actual = ms;
                    if (r != 0L) {
                        requestAmount = BackpressureHelper.addCap(requestAmount, r);
                        requestTarget = ms;
                    }
                } else if (a != null && mr != 0L) {
                    requestAmount = BackpressureHelper.addCap(requestAmount, mr);
                    requestTarget = a;
                }
            }

            missed = addAndGet(-missed);
            if (missed == 0) {
                if (requestAmount != 0L) {
                    requestTarget.request(requestAmount);
                }
                return;
            }
        }
    }
