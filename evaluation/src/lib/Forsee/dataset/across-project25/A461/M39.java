    public boolean addAll(@NonNull Disposable... disposables) {
        Objects.requireNonNull(disposables, "disposables is null");
        if (!disposed) {
            synchronized (this) {
                if (!disposed) {
                    OpenHashSet<Disposable> set = resources;
                    if (set == null) {
                        set = new OpenHashSet<>(disposables.length + 1);
                        resources = set;
                    }
                    for (Disposable d : disposables) {
                        Objects.requireNonNull(d, "A Disposable in the disposables array is null");
                        set.add(d);
                    }
                    return true;
                }
            }
        }
        for (Disposable d : disposables) {
            d.dispose();
        }
        return false;
    }
