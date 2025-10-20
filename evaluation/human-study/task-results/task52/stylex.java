    public static Map<String, String> toStringMap(Map<String, Object> input, boolean stringEscape) {
        Map<String, String> output = new HashMap<>(input.size());

        for (Map.Entry<String, Object> entry:input.entrySet()) {
            String key = entry.getKey();
            Object obj = entry.getValue();

            if (key == null || obj == null) {
                throw new NullPointerException("A null argument cannot be sent to Redis.");
            }

            String str;

            if (!(obj instanceof byte[])) {
                if (!(obj instanceof redis.clients.jedis.GeoCoordinate)) {
                    if (obj instanceof String) {
                        str = stringEscape ? escape((String) obj):(String) obj;
                    } else {
                        str = String.valueOf(obj);
                    }
                } else {
                    redis.clients.jedis.GeoCoordinate geo = (redis.clients.jedis.GeoCoordinate) obj;
                    str = geo.getLongitude() + "," + geo.getLatitude();
                }
            } else {
                str = SafeEncoder.encode((byte[]) obj);
            } 

            output.put(key, str);
        }
        return output;
    }
