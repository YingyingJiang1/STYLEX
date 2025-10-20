
    @Override
    public Connection getConnection() {
        final List<ConnectionPool> pools = getShuffledNodesPool();
        JedisException suppressed = null;
        for (ConnectionPool pool : pools) {
            Connection jedis = null;
            try {
                jedis = pool.getResource();
                if (jedis == null) {
                    continue;
                }
                jedis.ping();
                return jedis;
            } catch (JedisException ex) {
                if (suppressed == null) {
                    suppressed = ex;
                }
                if (jedis != null) {
                    jedis.close();
                }
            }
        }
        final JedisException noReachableNode = new JedisException("No reachable shard.");
        if (suppressed != null) {
            noReachableNode.addSuppressed(suppressed);
        }
        throw noReachableNode;
    }

