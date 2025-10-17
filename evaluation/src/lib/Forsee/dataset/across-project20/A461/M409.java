        @Override
        Node getHead() {
            long timeLimit = scheduler.now(unit) - maxAge;
            Node prev = get();
            Node next = prev.get();
            for (;;) {
                if (next == null) {
                    break;
                }
                Timed<?> v = (Timed<?>)next.value;
                if (NotificationLite.isComplete(v.value()) || NotificationLite.isError(v.value())) {
                    break;
                }
                if (v.time() <= timeLimit) {
                    prev = next;
                    next = next.get();
                } else {
                    break;
                }
            }
            return prev;
        }
