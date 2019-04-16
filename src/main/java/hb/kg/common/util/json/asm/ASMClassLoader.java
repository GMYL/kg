package hb.kg.common.util.json.asm;

import java.security.PrivilegedAction;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;

import hb.kg.common.exception.JSONException;
import hb.kg.common.exception.JSONPathException;
import hb.kg.common.util.file.IOUtils;
import hb.kg.common.util.json.JSON;
import hb.kg.common.util.json.JSONArray;
import hb.kg.common.util.json.JSONAware;
import hb.kg.common.util.json.JSONObject;
import hb.kg.common.util.json.JSONPath;
import hb.kg.common.util.json.JSONReader;
import hb.kg.common.util.json.JSONStreamAware;
import hb.kg.common.util.json.JSONWriter;
import hb.kg.common.util.json.TypeReference;
import hb.kg.common.util.json.parser.DefaultJSONParser;
import hb.kg.common.util.json.parser.Feature;
import hb.kg.common.util.json.parser.JSONLexer;
import hb.kg.common.util.json.parser.JSONLexerBase;
import hb.kg.common.util.json.parser.JSONReaderScanner;
import hb.kg.common.util.json.parser.JSONScanner;
import hb.kg.common.util.json.parser.JSONToken;
import hb.kg.common.util.json.parser.ParseContext;
import hb.kg.common.util.json.parser.ParserConfig;
import hb.kg.common.util.json.parser.SymbolTable;
import hb.kg.common.util.json.parser.deserializer.AutowiredObjectDeserializer;
import hb.kg.common.util.json.parser.deserializer.DefaultFieldDeserializer;
import hb.kg.common.util.json.parser.deserializer.ExtraProcessable;
import hb.kg.common.util.json.parser.deserializer.ExtraProcessor;
import hb.kg.common.util.json.parser.deserializer.ExtraTypeProvider;
import hb.kg.common.util.json.parser.deserializer.FieldDeserializer;
import hb.kg.common.util.json.parser.deserializer.JavaBeanDeserializer;
import hb.kg.common.util.json.parser.deserializer.ObjectDeserializer;
import hb.kg.common.util.json.serializer.AfterFilter;
import hb.kg.common.util.json.serializer.BeanContext;
import hb.kg.common.util.json.serializer.BeforeFilter;
import hb.kg.common.util.json.serializer.ContextObjectSerializer;
import hb.kg.common.util.json.serializer.ContextValueFilter;
import hb.kg.common.util.json.serializer.JSONSerializer;
import hb.kg.common.util.json.serializer.JavaBeanSerializer;
import hb.kg.common.util.json.serializer.LabelFilter;
import hb.kg.common.util.json.serializer.Labels;
import hb.kg.common.util.json.serializer.NameFilter;
import hb.kg.common.util.json.serializer.ObjectSerializer;
import hb.kg.common.util.json.serializer.PropertyFilter;
import hb.kg.common.util.json.serializer.PropertyPreFilter;
import hb.kg.common.util.json.serializer.SerialContext;
import hb.kg.common.util.json.serializer.SerializeBeanInfo;
import hb.kg.common.util.json.serializer.SerializeConfig;
import hb.kg.common.util.json.serializer.SerializeFilter;
import hb.kg.common.util.json.serializer.SerializeFilterable;
import hb.kg.common.util.json.serializer.SerializeWriter;
import hb.kg.common.util.json.serializer.SerializerFeature;
import hb.kg.common.util.json.serializer.ValueFilter;
import hb.kg.common.util.reflect.FieldInfo;
import hb.kg.common.util.reflect.JavaBeanInfo;
import hb.kg.common.util.reflect.ParameterizedTypeImpl;
import hb.kg.common.util.reflect.TypeUtils;

public class ASMClassLoader extends ClassLoader {
    private static java.security.ProtectionDomain DOMAIN;
    private static Map<String, Class<?>> classMapping = new HashMap<String, Class<?>>();
    static {
        DOMAIN = (java.security.ProtectionDomain) java.security.AccessController.doPrivileged(new PrivilegedAction<Object>() {
            public Object run() {
                return ASMClassLoader.class.getProtectionDomain();
            }
        });
        Class<?>[] jsonClasses = new Class<?>[] { JSON.class,
                                                  JSONObject.class,
                                                  JSONArray.class,
                                                  JSONPath.class,
                                                  JSONAware.class,
                                                  JSONException.class,
                                                  JSONPathException.class,
                                                  JSONReader.class,
                                                  JSONStreamAware.class,
                                                  JSONWriter.class,
                                                  TypeReference.class,
                                                  FieldInfo.class,
                                                  TypeUtils.class,
                                                  IOUtils.class,
                                                  IdentityHashMap.class,
                                                  ParameterizedTypeImpl.class,
                                                  JavaBeanInfo.class,
                                                  ObjectSerializer.class,
                                                  JavaBeanSerializer.class,
                                                  SerializeFilterable.class,
                                                  SerializeBeanInfo.class,
                                                  JSONSerializer.class,
                                                  SerializeWriter.class,
                                                  SerializeFilter.class,
                                                  Labels.class,
                                                  LabelFilter.class,
                                                  ContextValueFilter.class,
                                                  AfterFilter.class,
                                                  BeforeFilter.class,
                                                  NameFilter.class,
                                                  PropertyFilter.class,
                                                  PropertyPreFilter.class,
                                                  ValueFilter.class,
                                                  SerializerFeature.class,
                                                  ContextObjectSerializer.class,
                                                  SerialContext.class,
                                                  SerializeConfig.class,
                                                  JavaBeanDeserializer.class,
                                                  ParserConfig.class,
                                                  DefaultJSONParser.class,
                                                  JSONLexer.class,
                                                  JSONLexerBase.class,
                                                  ParseContext.class,
                                                  JSONToken.class,
                                                  SymbolTable.class,
                                                  Feature.class,
                                                  JSONScanner.class,
                                                  JSONReaderScanner.class,
                                                  AutowiredObjectDeserializer.class,
                                                  ObjectDeserializer.class,
                                                  ExtraProcessor.class,
                                                  ExtraProcessable.class,
                                                  ExtraTypeProvider.class,
                                                  BeanContext.class,
                                                  FieldDeserializer.class,
                                                  DefaultFieldDeserializer.class, };
        for (Class<?> clazz : jsonClasses) {
            classMapping.put(clazz.getName(), clazz);
        }
    }

    public ASMClassLoader() {
        super(getParentClassLoader());
    }

    public ASMClassLoader(ClassLoader parent) {
        super(parent);
    }

    static ClassLoader getParentClassLoader() {
        ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        if (contextClassLoader != null) {
            try {
                contextClassLoader.loadClass(JSON.class.getName());
                return contextClassLoader;
            } catch (ClassNotFoundException e) {
                // skip
            }
        }
        return JSON.class.getClassLoader();
    }

    protected Class<?> loadClass(String name,
                                 boolean resolve)
            throws ClassNotFoundException {
        Class<?> mappingClass = classMapping.get(name);
        if (mappingClass != null) {
            return mappingClass;
        }
        try {
            return super.loadClass(name, resolve);
        } catch (ClassNotFoundException e) {
            throw e;
        }
    }

    public Class<?> defineClassPublic(String name,
                                      byte[] b,
                                      int off,
                                      int len)
            throws ClassFormatError {
        Class<?> clazz = defineClass(name, b, off, len, DOMAIN);
        return clazz;
    }

    public boolean isExternalClass(Class<?> clazz) {
        ClassLoader classLoader = clazz.getClassLoader();
        if (classLoader == null) {
            return false;
        }
        ClassLoader current = this;
        while (current != null) {
            if (current == classLoader) {
                return false;
            }
            current = current.getParent();
        }
        return true;
    }
}
