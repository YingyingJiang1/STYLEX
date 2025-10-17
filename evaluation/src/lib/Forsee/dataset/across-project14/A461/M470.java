    void replay(CacheDisposable<T> consumer) {
        // make sure there is only one replay going on at a time
        if (consumer.getAndIncrement() != 0) {
            return;
        }

        // see if there were more replay request in the meantime
        int missed = 1;
        // read out state into locals upfront to avoid being re-read due to volatile reads
        long index = consumer.index;
        int offset = consumer.offset;
        Node<T> node = consumer.node;
        Observer<? super T> downstream = consumer.downstream;
        int capacity = capacityHint;

        for (;;) {
            // if the consumer got disposed, clear the node and quit
            if (consumer.disposed) {
                consumer.node = null;
                return;
            }

            // first see if the source has terminated, read order matters!
            boolean sourceDone = done;
            // and if the number of items is the same as this consumer has received
            boolean empty = size == index;

            // if the source is done and we have all items so far, terminate the consumer
            if (sourceDone && empty) {
                // release the node object to avoid leaks through retained consumers
                consumer.node = null;
                // if error is not null then the source failed
                Throwable ex = error;
                if (ex != null) {
                    downstream.onError(ex);
                } else {
                    downstream.onComplete();
                }
                return;
            }

            // there are still items not sent to the consumer
            if (!empty) {
             // if the offset in the current node has reached the node capacity
                if (offset == capacity) {
                    // switch to the subsequent node
                    node = node.next;
                    // reset the in-node offset
                    offset = 0;
                }

                // emit the cached item
                downstream.onNext(node.values[offset]);

                // move the node offset forward
                offset++;
                // move the total consumed item count forward
                index++;

                // retry for the next item/terminal event if any
                continue;
            }

            // commit the changed references back
            consumer.index = index;
            consumer.offset = offset;
            consumer.node = node;
            // release the changes and see if there were more replay request in the meantime
            missed = consumer.addAndGet(-missed);
            if (missed == 0) {
                break;
            }
        }
    }
