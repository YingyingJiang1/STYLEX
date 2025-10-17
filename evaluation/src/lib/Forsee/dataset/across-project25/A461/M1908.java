        void drain() {
            if (getAndIncrement() != 0) {
                return;
            }

            int missed = 1;

            final Subscriber<? super R> downstream = this.downstream;
            final SimpleQueue<T> queue = this.queue;
            final AtomicThrowable error = this.error;
            Iterator<? extends R> iterator = this.currentIterator;
            long requested = this.requested.get();
            long emitted = this.emitted;
            final int limit = prefetch - (prefetch >> 2);
            boolean canRequest = sourceMode != QueueFuseable.SYNC;

            for (;;) {
                if (cancelled) {
                    queue.clear();
                    clearCurrentSuppressCloseError();
                } else {
                    boolean isDone = upstreamDone;
                    if (error.get() != null) {
                        downstream.onError(error.get());
                        cancelled = true;
                        continue;
                    }

                    if (iterator == null) {
                        T t;

                        try {
                            t = queue.poll();
                        } catch (Throwable ex) {
                            Exceptions.throwIfFatal(ex);
                            trySignalError(downstream, ex);
                            continue;
                        }

                        boolean isEmpty = t == null;

                        if (isDone && isEmpty) {
                            downstream.onComplete();
                            cancelled = true;
                        }
                        else if (!isEmpty) {
                            if (canRequest && ++consumed == limit) {
                                consumed = 0;
                                upstream.request(limit);
                            }

                            Stream<? extends R> stream;
                            try {
                                stream = Objects.requireNonNull(mapper.apply(t), "The mapper returned a null Stream");
                                iterator = stream.iterator();

                                if (iterator.hasNext()) {
                                    currentIterator = iterator;
                                    currentCloseable = stream;
                                } else {
                                    iterator = null;
                                }
                            } catch (Throwable ex) {
                                Exceptions.throwIfFatal(ex);
                                trySignalError(downstream, ex);
                            }
                            continue;
                        }
                    }
                    if (iterator != null && emitted != requested) {
                        R item;

                        try {
                            item = Objects.requireNonNull(iterator.next(), "The Stream.Iterator returned a null value");
                        } catch (Throwable ex) {
                            Exceptions.throwIfFatal(ex);
                            trySignalError(downstream, ex);
                            continue;
                        }

                        if (!cancelled) {
                            downstream.onNext(item);
                            emitted++;

                            if (!cancelled) {
                                try {
                                    if (!iterator.hasNext()) {
                                        iterator = null;
                                        clearCurrentRethrowCloseError();
                                    }
                                } catch (Throwable ex) {
                                    Exceptions.throwIfFatal(ex);
                                    trySignalError(downstream, ex);
                                }
                            }
                        }

                        continue;
                    }
                }

                this.emitted = emitted;
                missed = addAndGet(-missed);
                if (missed == 0) {
                    break;
                }
                requested = this.requested.get();
            }
        }
