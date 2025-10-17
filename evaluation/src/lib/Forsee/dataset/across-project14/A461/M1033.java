        void drain() {
            if (getAndIncrement() != 0) {
                return;
            }

            final Subscriber<? super R> a = downstream;
            final SimpleQueue<T> q = queue;
            final boolean replenish = fusionMode != SYNC;

            int missed = 1;

            Iterator<? extends R> it = current;

            for (;;) {

                if (it == null) {

                    boolean d = done;

                    T t;

                    try {
                        t = q.poll();
                    } catch (Throwable ex) {
                        Exceptions.throwIfFatal(ex);
                        upstream.cancel();
                        ExceptionHelper.addThrowable(error, ex);
                        ex = ExceptionHelper.terminate(error);

                        current = null;
                        q.clear();

                        a.onError(ex);
                        return;
                    }

                    boolean empty = t == null;

                    if (checkTerminated(d, empty, a, q)) {
                        return;
                    }

                    if (t != null) {
                        Iterable<? extends R> iterable;

                        boolean b;

                        try {
                            iterable = mapper.apply(t);

                            it = iterable.iterator();

                            b = it.hasNext();
                        } catch (Throwable ex) {
                            Exceptions.throwIfFatal(ex);
                            upstream.cancel();
                            ExceptionHelper.addThrowable(error, ex);
                            ex = ExceptionHelper.terminate(error);
                            a.onError(ex);
                            return;
                        }

                        if (!b) {
                            it = null;
                            consumedOne(replenish);
                            continue;
                        }

                        current = it;
                    }
                }

                if (it != null) {
                    long r = requested.get();
                    long e = 0L;

                    while (e != r) {
                        if (checkTerminated(done, false, a, q)) {
                            return;
                        }

                        R v;

                        try {
                            v = Objects.requireNonNull(it.next(), "The iterator returned a null value");
                        } catch (Throwable ex) {
                            Exceptions.throwIfFatal(ex);
                            current = null;
                            upstream.cancel();
                            ExceptionHelper.addThrowable(error, ex);
                            ex = ExceptionHelper.terminate(error);
                            a.onError(ex);
                            return;
                        }

                        a.onNext(v);

                        if (checkTerminated(done, false, a, q)) {
                            return;
                        }

                        e++;

                        boolean b;

                        try {
                            b = it.hasNext();
                        } catch (Throwable ex) {
                            Exceptions.throwIfFatal(ex);
                            current = null;
                            upstream.cancel();
                            ExceptionHelper.addThrowable(error, ex);
                            ex = ExceptionHelper.terminate(error);
                            a.onError(ex);
                            return;
                        }

                        if (!b) {
                            consumedOne(replenish);
                            it = null;
                            current = null;
                            break;
                        }
                    }

                    if (e == r) {
                        boolean d = done;
                        boolean empty = q.isEmpty() && it == null;

                        if (checkTerminated(d, empty, a, q)) {
                            return;
                        }
                    }

                    if (e != 0L) {
                        if (r != Long.MAX_VALUE) {
                            requested.addAndGet(-e);
                        }
                    }

                    if (it == null) {
                        continue;
                    }
                }

                missed = addAndGet(-missed);
                if (missed == 0) {
                    break;
                }
            }
        }
