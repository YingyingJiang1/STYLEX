        void drain() {
            if (getAndIncrement() != 0) {
                return;
            }

            int missed = 1;
            Subscriber<? super R> downstream = this.downstream;
            long emitted = this.emitted;
            long requested = this.requested.get();
            Iterator<? extends R> it = iterator;

            for (;;) {

                if (cancelled) {
                    clear();
                } else {
                    if (outputFused) {
                        if (it != null) {
                            downstream.onNext(null);
                            downstream.onComplete();
                        }
                    } else {
                        if (it != null && emitted != requested) {
                            R item;
                            try {
                                item = it.next();
                            } catch (Throwable ex) {
                                Exceptions.throwIfFatal(ex);
                                downstream.onError(ex);
                                cancelled = true;
                                continue;
                            }

                            if (cancelled) {
                                continue;
                            }

                            downstream.onNext(item);
                            emitted++;

                            if (cancelled) {
                                continue;
                            }

                            boolean has;
                            try {
                                has = it.hasNext();
                            } catch (Throwable ex) {
                                Exceptions.throwIfFatal(ex);
                                downstream.onError(ex);
                                cancelled = true;
                                continue;
                            }

                            if (cancelled) {
                                continue;
                            }

                            if (!has) {
                                downstream.onComplete();
                                cancelled = true;
                            }
                            continue;
                        }
                    }
                }

                this.emitted = emitted;
                missed = addAndGet(-missed);
                if (missed == 0) {
                    return;
                }

                requested = this.requested.get();
                if (it == null) {
                    it = iterator;
                }
            }
        }
