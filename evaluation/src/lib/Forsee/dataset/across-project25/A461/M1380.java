        @Override
        public void onComplete() {
            if (done) {
                return;
            }
            try {
                onComplete.run();
            } catch (Throwable e) {
                fail(e);
                return;
            }

            done = true;
            downstream.onComplete();

            try {
                onAfterTerminate.run();
            } catch (Throwable e) {
                Exceptions.throwIfFatal(e);
                RxJavaPlugins.onError(e);
            }
        }
