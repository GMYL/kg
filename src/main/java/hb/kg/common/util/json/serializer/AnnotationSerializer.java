package hb.kg.common.util.json.serializer;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Iterator;
import java.util.Map;

import hb.kg.common.util.json.JSON;
import hb.kg.common.util.json.JSONObject;
import sun.reflect.annotation.AnnotationType;

@SuppressWarnings({ "rawtypes", "restriction", "unchecked" })
public class AnnotationSerializer implements ObjectSerializer {
    public static AnnotationSerializer instance = new AnnotationSerializer();

    public void write(JSONSerializer serializer,
                      Object object,
                      Object fieldName,
                      Type fieldType,
                      int features)
            throws IOException {
        Class objClass = object.getClass();
        Class[] interfaces = objClass.getInterfaces();
        if (interfaces.length == 1 && interfaces[0].isAnnotation()) {
            Class annotationClass = interfaces[0];
            AnnotationType type = AnnotationType.getInstance(annotationClass);
            Map<String, Method> members = type.members();
            JSONObject json = new JSONObject(members.size());
            Iterator<Map.Entry<String, Method>> iterator = members.entrySet().iterator();
            Map.Entry<String, Method> entry;
            Object val = null;
            while (iterator.hasNext()) {
                entry = iterator.next();
                try {
                    val = entry.getValue().invoke(object);
                } catch (IllegalAccessException e) {
                    // skip
                } catch (InvocationTargetException e) {
                    // skip
                }
                json.put(entry.getKey(), JSON.toJSON(val));
            }
            serializer.write(json);
            return;
        }
    }
}
