    @BackpressureSupport(BackpressureKind.SPECIAL)
    @CheckReturnValue
    @SchedulerSupport(SchedulerSupport.NONE)
    @NonNull
    public static <@NonNull T> Flowable<T> fromObservable(@NonNull ObservableSource<T> source, @NonNull BackpressureStrategy strategy) {
        Objects.requireNonNull(source, "source is null");
        Objects.requireNonNull(strategy, "strategy is null");
        Flowable<T> f = new FlowableFromObservable<>(source);
        switch (strategy) {
            case DROP:
                return f.onBackpressureDrop();
            case LATEST:
                return f.onBackpressureLatest();
            case MISSING:
                return f;
            case ERROR:
                return RxJavaPlugins.onAssembly(new FlowableOnBackpressureError<>(f));
            default:
                return f.onBackpressureBuffer();
        }
    }
