        @SuppressWarnings("unchecked")
        void drain() {
            if (getAndIncrement() != 0) {
                return;
            }

            Observer<? super R> downstream = this.downstream;
            SimpleQueue<T> queue = this.queue;
            AtomicThrowable errors = this.errors;

            for (;;) {

                if (!active) {

                    if (cancelled) {
                        queue.clear();
                        return;
                    }

                    if (!tillTheEnd) {
                        Throwable ex = errors.get();
                        if (ex != null) {
                            queue.clear();
                            cancelled = true;
                            errors.tryTerminateConsumer(downstream);
                            return;
                        }
                    }

                    boolean d = done;

                    T v;

                    try {
                        v = queue.poll();
                    } catch (Throwable ex) {
                        Exceptions.throwIfFatal(ex);
                        cancelled = true;
                        this.upstream.dispose();
                        errors.tryAddThrowableOrReport(ex);
                        errors.tryTerminateConsumer(downstream);
                        return;
                    }

                    boolean empty = v == null;

                    if (d && empty) {
                        cancelled = true;
                        errors.tryTerminateConsumer(downstream);
                        return;
                    }

                    if (!empty) {

                        ObservableSource<? extends R> o;

                        try {
                            o = Objects.requireNonNull(mapper.apply(v), "The mapper returned a null ObservableSource");
                        } catch (Throwable ex) {
                            Exceptions.throwIfFatal(ex);
                            cancelled = true;
                            this.upstream.dispose();
                            queue.clear();
                            errors.tryAddThrowableOrReport(ex);
                            errors.tryTerminateConsumer(downstream);
                            return;
                        }

                        if (o instanceof Supplier) {
                            R w;

                            try {
                                w = ((Supplier<R>)o).get();
                            } catch (Throwable ex) {
                                Exceptions.throwIfFatal(ex);
                                errors.tryAddThrowableOrReport(ex);
                                continue;
                            }

                            if (w != null && !cancelled) {
                                downstream.onNext(w);
                            }
                            continue;
                        } else {
                            active = true;
                            o.subscribe(observer);
                        }
                    }
                }

                if (decrementAndGet() == 0) {
                    break;
                }
            }
        }
