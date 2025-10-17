
@Override
public TSInfo build(Object data) {
    List<KeyValue> list = (List<KeyValue>) data;
    Map<String, Object> properties = new HashMap<>();
    Map<String, String> labels = null;
    Map<String, Rule> rules = null;
    List<Map<String, Object>> chunks = null;

    for (KeyValue propertyValue : list) {
        String prop = BuilderFactory.STRING.build(propertyValue.getKey());
        Object value = propertyValue.getValue();
        
        if (value instanceof List) {
            switch (prop) {
                case LABELS_PROPERTY:
                    labels = BuilderFactory.STRING_MAP.build(value);
                    value = labels;
                    break;
                case RULES_PROPERTY:
                    List<KeyValue> rulesDataList = (List<KeyValue>) value;
                    rules = new HashMap<>(rulesDataList.size());
                    for (KeyValue rkv : rulesDataList) {
                        String ruleName = BuilderFactory.STRING.build(rkv.getKey());
                        List<Object> ruleValueList = BuilderFactory.ENCODED_OBJECT_LIST.build(rkv.getValue());
                        rules.put(ruleName, new Rule(ruleName, ruleValueList));
                    }
                    value = rules;
                    break;
                case CHUNKS_PROPERTY:
                    List<List<KeyValue>> chunksDataList = (List<List<KeyValue>>) value;
                    chunks = new ArrayList<>(chunksDataList.size());
                    for (List<KeyValue> chunkDataAsList : chunksDataList) {
                        Map<String, Object> chunk = chunkDataAsList.stream()
                            .collect(Collectors.toMap(
                                kv -> BuilderFactory.STRING.build(kv.getKey()),
                                kv -> BuilderFactory.ENCODED_OBJECT.build(kv.getValue())
                            ));
                        chunks.add(chunk);
                    }
                    value = chunks;
                    break;
                default:
                    value = SafeEncoder.encodeObject(value);
            }
        } else if (value instanceof byte[]) {
            value = BuilderFactory.STRING.build(value);
            if (DUPLICATE_POLICY_PROPERTY.equals(prop)) {
                try {
                    value = DuplicatePolicy.valueOf(((String) value).toUpperCase());
                } catch (Exception e) {
                    // Ignore conversion failure
                }
            }
        }
        properties.put(prop, value);
    }

    return new TSInfo(properties, labels, rules, chunks);
}
