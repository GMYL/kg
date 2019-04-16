package hb.kg.common.util.json.serializer;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;

import hb.kg.common.util.reflect.TypeUtils;

public final class ListSerializer implements ObjectSerializer {
    public static final ListSerializer instance = new ListSerializer();

    public final void write(JSONSerializer serializer,
                            Object object,
                            Object fieldName,
                            Type fieldType,
                            int features)
            throws IOException {
        boolean writeClassName = serializer.out.isEnabled(SerializerFeature.WriteClassName)
                || SerializerFeature.isEnabled(features, SerializerFeature.WriteClassName);
        SerializeWriter out = serializer.out;
        Type elementType = null;
        if (writeClassName) {
            elementType = TypeUtils.getCollectionItemType(fieldType);
        }
        if (object == null) {
            out.writeNull(SerializerFeature.WriteNullListAsEmpty);
            return;
        }
        List<?> list = (List<?>) object;
        if (list.size() == 0) {
            out.append("[]");
            return;
        }
        SerialContext context = serializer.context;
        serializer.setContext(context, object, fieldName, 0);
        ObjectSerializer itemSerializer = null;
        try {
            if (out.isEnabled(SerializerFeature.PrettyFormat)) {
                out.append('[');
                serializer.incrementIndent();
                int i = 0;
                for (Object item : list) {
                    if (i != 0) {
                        out.append(',');
                    }
                    serializer.println();
                    if (item != null) {
                        if (serializer.containsReference(item)) {
                            serializer.writeReference(item);
                        } else {
                            itemSerializer = serializer.getObjectWriter(item.getClass());
                            SerialContext itemContext = new SerialContext(context,
                                                                          object,
                                                                          fieldName,
                                                                          0,
                                                                          0);
                            serializer.context = itemContext;
                            itemSerializer.write(serializer, item, i, elementType, features);
                        }
                    } else {
                        serializer.out.writeNull();
                    }
                    i++;
                }
                serializer.decrementIdent();
                serializer.println();
                out.append(']');
                return;
            }
            out.append('[');
            for (int i = 0, size = list.size(); i < size; ++i) {
                Object item = list.get(i);
                if (i != 0) {
                    out.append(',');
                }
                if (item == null) {
                    out.append("null");
                } else {
                    Class<?> clazz = item.getClass();
                    if (clazz == Integer.class) {
                        out.writeInt(((Integer) item).intValue());
                    } else if (clazz == Long.class) {
                        long val = ((Long) item).longValue();
                        if (writeClassName) {
                            out.writeLong(val);
                            out.write('L');
                        } else {
                            out.writeLong(val);
                        }
                    } else {
                        if ((SerializerFeature.DisableCircularReferenceDetect.mask
                                & features) != 0) {
                            itemSerializer = serializer.getObjectWriter(item.getClass());
                            itemSerializer.write(serializer, item, i, elementType, features);
                        } else {
                            if (!out.disableCircularReferenceDetect) {
                                SerialContext itemContext = new SerialContext(context,
                                                                              object,
                                                                              fieldName,
                                                                              0,
                                                                              0);
                                serializer.context = itemContext;
                            }
                            if (serializer.containsReference(item)) {
                                serializer.writeReference(item);
                            } else {
                                itemSerializer = serializer.getObjectWriter(item.getClass());
                                if ((SerializerFeature.WriteClassName.mask & features) != 0
                                        && itemSerializer instanceof JavaBeanSerializer) {
                                    JavaBeanSerializer javaBeanSerializer = (JavaBeanSerializer) itemSerializer;
                                    javaBeanSerializer.writeNoneASM(serializer,
                                                                    item,
                                                                    i,
                                                                    elementType,
                                                                    features);
                                } else {
                                    itemSerializer.write(serializer,
                                                         item,
                                                         i,
                                                         elementType,
                                                         features);
                                }
                            }
                        }
                    }
                }
            }
            out.append(']');
        } finally {
            serializer.context = context;
        }
    }
}
