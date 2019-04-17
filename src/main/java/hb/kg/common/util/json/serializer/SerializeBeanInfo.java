package hb.kg.common.util.json.serializer;

import hb.kg.common.util.json.anno.JSONType;
import hb.kg.common.util.reflect.FieldInfo;

public class SerializeBeanInfo {
    protected final Class<?> beanType;
    protected final String typeName;
    protected final String typeKey;
    protected final JSONType jsonType;
    protected final FieldInfo[] fields;
    protected final FieldInfo[] sortedFields;
    protected int features;

    public SerializeBeanInfo(Class<?> beanType, //
                             JSONType jsonType, //
                             String typeName, //
                             String typeKey,
                             int features,
                             FieldInfo[] fields, //
                             FieldInfo[] sortedFields) {
        this.beanType = beanType;
        this.jsonType = jsonType;
        this.typeName = typeName;
        this.typeKey = typeKey;
        this.features = features;
        this.fields = fields;
        this.sortedFields = sortedFields;
    }
}