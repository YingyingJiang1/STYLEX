            @Override
            public void dispose() {
                for (;;) {
                    int state = get();
                    if (state >= FINISHED) {
                        break;
                    } else if (state == READY) {
                        if (compareAndSet(READY, INTERRUPTED)) {
                            cleanup();
                            break;
                        }
                    } else {
                        if (compareAndSet(RUNNING, INTERRUPTING)) {
                            Thread t = thread;
                            if (t != null) {
                                t.interrupt();
                                thread = null;
                            }
                            set(INTERRUPTED);
                            cleanup();
                            break;
                        }
                    }
                }
            }
