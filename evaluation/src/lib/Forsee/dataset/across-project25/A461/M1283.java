        void drain() {
            if (getAndIncrement() != 0) {
                return;
            }

            int missed = 1;
            SimpleQueue<T> queue = this.queue;
            int consumed = this.consumed;
            int limit = this.bufferSize - (this.bufferSize >> 2);
            boolean async = this.sourceMode != QueueSubscription.SYNC;

            outer:
            for (;;) {
                if (queue != null) {
                    long minDemand = Long.MAX_VALUE;
                    boolean hasDemand = false;

                    InnerSubscription<T>[] consumers = subscribers.get();

                    for (InnerSubscription<T> inner : consumers) {
                        long request = inner.get();
                        if (request != Long.MIN_VALUE) {
                            hasDemand = true;
                            minDemand = Math.min(request - inner.emitted, minDemand);
                        }
                    }

                    if (!hasDemand) {
                        minDemand = 0L;
                    }

                    while (minDemand != 0L) {
                        boolean d = done;
                        T v;

                        try {
                            v = queue.poll();
                        } catch (Throwable ex) {
                            Exceptions.throwIfFatal(ex);
                            upstream.get().cancel();
                            queue.clear();
                            done = true;
                            signalError(ex);
                            return;
                        }

                        boolean empty = v == null;

                        if (checkTerminated(d, empty)) {
                            return;
                        }

                        if (empty) {
                            break;
                        }

                        for (InnerSubscription<T> inner : consumers) {
                            if (!inner.isCancelled()) {
                                inner.downstream.onNext(v);
                                inner.emitted++;
                            }
                        }

                        if (async && ++consumed == limit) {
                            consumed = 0;
                            upstream.get().request(limit);
                        }
                        minDemand--;

                        if (consumers != subscribers.get()) {
                            continue outer;
                        }
                    }

                    if (checkTerminated(done, queue.isEmpty())) {
                        return;
                    }
                }

                this.consumed = consumed;
                missed = addAndGet(-missed);
                if (missed == 0) {
                    break;
                }
                if (queue == null) {
                    queue = this.queue;
                }
            }
        }
