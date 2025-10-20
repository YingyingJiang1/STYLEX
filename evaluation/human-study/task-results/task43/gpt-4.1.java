
    @Override
    public CacheEntry set(CacheKey cacheKey, CacheEntry entry) {
        lock.lock();
        try {
            entry = putIntoStore(cacheKey, entry);
            final EvictionPolicy policy = getEvictionPolicy();
            policy.touch(cacheKey);
            final CacheKey evictedKey = policy.evictNext();
            if (evictedKey != null) {
                delete(evictedKey);
                stats.evict();
            }
            for (Object redisKey : cacheKey.getRedisKeys()) {
                final ByteBuffer mapKey = makeKeyForRedisKeysToCacheKeys(redisKey);
                if (redisKeysToCacheKeys.containsKey(mapKey)) {
                    redisKeysToCacheKeys.get(mapKey).add(cacheKey);
                } else {
                    final Set<CacheKey<?>> set = ConcurrentHashMap.newKeySet();
                    set.add(cacheKey);
                    redisKeysToCacheKeys.put(mapKey, set);
                }
            }
            stats.load();
            return entry;
        } finally {
            lock.unlock();
        }
    }

