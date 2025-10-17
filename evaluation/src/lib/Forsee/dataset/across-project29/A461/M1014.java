        void requestGroup(long n) {
            // lots of atomics, save local
            AtomicLong groupConsumed = this.groupConsumed;
            int limit = this.limit;
            // Concurrent groups can request at once, a CAS loop is needed
            for (;;) {
                long currentConsumed = groupConsumed.get();
                long newConsumed = BackpressureHelper.addCap(currentConsumed, n);
                // Accumulate the consumed amounts and atomically update the total
                if (groupConsumed.compareAndSet(currentConsumed, newConsumed)) {
                    // if successful, let's see if the prefetch limit has been surpassed
                    for (;;) {
                        if (newConsumed < limit) {
                            // no further actions to be taken
                            return;
                        }

                        // Yes, remove one limit from total consumed
                        long newConsumedAfterLimit = newConsumed - limit;
                        // Only one thread should subtract
                        if (groupConsumed.compareAndSet(newConsumed, newConsumedAfterLimit)) {
                            // Then request up to limit
                            upstream.request(limit);
                        }
                        // We don't quit but loop to see if we are still above the prefetch limit
                        newConsumed = groupConsumed.get();
                    }
                }
            }
        }
