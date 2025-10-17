    @Override
    public void subscribeActual(Subscriber<? super T> t) {
        BaseEmitter<T> emitter;

        switch (backpressure) {
        case MISSING: {
            emitter = new MissingEmitter<>(t);
            break;
        }
        case ERROR: {
            emitter = new ErrorAsyncEmitter<>(t);
            break;
        }
        case DROP: {
            emitter = new DropAsyncEmitter<>(t);
            break;
        }
        case LATEST: {
            emitter = new LatestAsyncEmitter<>(t);
            break;
        }
        default: {
            emitter = new BufferAsyncEmitter<>(t, bufferSize());
            break;
        }
        }

        t.onSubscribe(emitter);
        try {
            source.subscribe(emitter);
        } catch (Throwable ex) {
            Exceptions.throwIfFatal(ex);
            emitter.onError(ex);
        }
    }
