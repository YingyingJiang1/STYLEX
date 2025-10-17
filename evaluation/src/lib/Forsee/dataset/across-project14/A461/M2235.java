    @Override
    @NonNull
    public synchronized Throwable getCause() { // NOPMD
        if (cause == null) {
            String separator = System.getProperty("line.separator");
            if (exceptions.size() > 1) {
                Map<Throwable, Boolean> seenCauses = new IdentityHashMap<>();

                StringBuilder aggregateMessage = new StringBuilder();
                aggregateMessage.append("Multiple exceptions (").append(exceptions.size()).append(")").append(separator);

                for (Throwable inner : exceptions) {
                    int depth = 0;
                    while (inner != null) {
                        for (int i = 0; i < depth; i++) {
                            aggregateMessage.append("  ");
                        }
                        aggregateMessage.append("|-- ");
                        aggregateMessage.append(inner.getClass().getCanonicalName()).append(": ");
                        String innerMessage = inner.getMessage();
                        if (innerMessage != null && innerMessage.contains(separator)) {
                            aggregateMessage.append(separator);
                            for (String line : innerMessage.split(separator)) {
                                for (int i = 0; i < depth + 2; i++) {
                                    aggregateMessage.append("  ");
                                }
                                aggregateMessage.append(line).append(separator);
                            }
                        } else {
                            aggregateMessage.append(innerMessage);
                            aggregateMessage.append(separator);
                        }

                        for (int i = 0; i < depth + 2; i++) {
                            aggregateMessage.append("  ");
                        }
                        StackTraceElement[] st = inner.getStackTrace();
                        if (st.length > 0) {
                            aggregateMessage.append("at ").append(st[0]).append(separator);
                        }

                        if (!seenCauses.containsKey(inner)) {
                            seenCauses.put(inner, true);

                            inner = inner.getCause();
                            depth++;
                        } else {
                            inner = inner.getCause();
                            if (inner != null) {
                                for (int i = 0; i < depth + 2; i++) {
                                    aggregateMessage.append("  ");
                                }
                                aggregateMessage.append("|-- ");
                                aggregateMessage.append("(cause not expanded again) ");
                                aggregateMessage.append(inner.getClass().getCanonicalName()).append(": ");
                                aggregateMessage.append(inner.getMessage());
                                aggregateMessage.append(separator);
                            }
                            break;
                        }
                    }
                }

                cause = new ExceptionOverview(aggregateMessage.toString().trim());
            } else {
                cause = exceptions.get(0);
            }
        }
        return cause;
    }
