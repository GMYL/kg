package hb.kg.common.util.json.serializer;

import java.io.File;
import java.io.Serializable;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URL;
import java.nio.charset.Charset;
import java.sql.Clob;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Currency;
import java.util.Date;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.TimeZone;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicIntegerArray;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicLongArray;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;

import javax.xml.datatype.XMLGregorianCalendar;

import hb.kg.common.exception.JSONException;
import hb.kg.common.util.json.JSON;
import hb.kg.common.util.json.JSONAware;
import hb.kg.common.util.json.JSONStreamAware;
import hb.kg.common.util.json.PropertyNamingStrategy;
import hb.kg.common.util.json.anno.JSONField;
import hb.kg.common.util.json.anno.JSONType;
import hb.kg.common.util.json.asm.ASMUtils;
import hb.kg.common.util.json.parser.deserializer.Jdk8DateCodec;
import hb.kg.common.util.json.parser.deserializer.OptionalCodec;
import hb.kg.common.util.json.spring.SwaggerJsonSerializer;
import hb.kg.common.util.reflect.FieldInfo;
import hb.kg.common.util.reflect.TypeUtils;
import hb.kg.common.util.set.IdentityHashMap;

public class SerializeConfig {
    public final static SerializeConfig globalInstance = new SerializeConfig();
    private static boolean awtError = false;
    private static boolean jdk8Error = false;
    private static boolean oracleJdbcError = false;
    private static boolean springfoxError = false;
    private static boolean guavaError = false;
    private static boolean jsonnullError = false;
    private static boolean jodaError = false;
    private boolean asm = !ASMUtils.IS_ANDROID;
    private ASMSerializerFactory asmFactory;
    protected String typeKey = JSON.DEFAULT_TYPE_KEY;
    public PropertyNamingStrategy propertyNamingStrategy;
    private final IdentityHashMap<Type, ObjectSerializer> serializers;
    private final boolean fieldBased;

    public String getTypeKey() {
        return typeKey;
    }

    public void setTypeKey(String typeKey) {
        this.typeKey = typeKey;
    }

    private final JavaBeanSerializer createASMSerializer(SerializeBeanInfo beanInfo)
            throws Exception {
        JavaBeanSerializer serializer = asmFactory.createJavaBeanSerializer(beanInfo);
        for (int i = 0; i < serializer.sortedGetters.length; ++i) {
            FieldSerializer fieldDeser = serializer.sortedGetters[i];
            Class<?> fieldClass = fieldDeser.fieldInfo.fieldClass;
            if (fieldClass.isEnum()) {
                ObjectSerializer fieldSer = this.getObjectWriter(fieldClass);
                if (!(fieldSer instanceof EnumSerializer)) {
                    serializer.writeDirect = false;
                }
            }
        }
        return serializer;
    }

    public final ObjectSerializer createJavaBeanSerializer(Class<?> clazz) {
        SerializeBeanInfo beanInfo = TypeUtils.buildBeanInfo(clazz,
                                                             null,
                                                             propertyNamingStrategy,
                                                             fieldBased);
        if (beanInfo.fields.length == 0 && Iterable.class.isAssignableFrom(clazz)) {
            return MiscCodec.instance;
        }
        return createJavaBeanSerializer(beanInfo);
    }

    public ObjectSerializer createJavaBeanSerializer(SerializeBeanInfo beanInfo) {
        JSONType jsonType = beanInfo.jsonType;
        boolean asm = this.asm && !fieldBased;
        if (jsonType != null) {
            Class<?> serializerClass = jsonType.serializer();
            if (serializerClass != Void.class) {
                try {
                    Object seralizer = serializerClass.newInstance();
                    if (seralizer instanceof ObjectSerializer) {
                        return (ObjectSerializer) seralizer;
                    }
                } catch (Throwable e) {
                    // skip
                }
            }
            if (jsonType.asm() == false) {
                asm = false;
            }
            if (asm) {
                for (SerializerFeature feature : jsonType.serialzeFeatures()) {
                    if (SerializerFeature.WriteNonStringValueAsString == feature //
                            || SerializerFeature.WriteEnumUsingToString == feature //
                            || SerializerFeature.NotWriteDefaultValue == feature
                            || SerializerFeature.BrowserCompatible == feature) {
                        asm = false;
                        break;
                    }
                }
            }
            if (asm) {
                final Class<? extends SerializeFilter>[] filterClasses = jsonType.serialzeFilters();
                if (filterClasses.length != 0) {
                    asm = false;
                }
            }
        }
        Class<?> clazz = beanInfo.beanType;
        if (!Modifier.isPublic(beanInfo.beanType.getModifiers())) {
            return new JavaBeanSerializer(beanInfo);
        }
        if (asm && asmFactory.classLoader.isExternalClass(clazz) || clazz == Serializable.class
                || clazz == Object.class) {
            asm = false;
        }
        if (asm && !ASMUtils.checkName(clazz.getSimpleName())) {
            asm = false;
        }
        if (asm && beanInfo.beanType.isInterface()) {
            asm = false;
        }
        if (asm) {
            for (FieldInfo fieldInfo : beanInfo.fields) {
                Field field = fieldInfo.field;
                if (field != null && !field.getType().equals(fieldInfo.fieldClass)) {
                    asm = false;
                    break;
                }
                Method method = fieldInfo.method;
                if (method != null && !method.getReturnType().equals(fieldInfo.fieldClass)) {
                    asm = false;
                    break;
                }
                JSONField annotation = fieldInfo.getAnnotation();
                if (annotation == null) {
                    continue;
                }
                String format = annotation.format();
                if (format.length() != 0) {
                    if (fieldInfo.fieldClass == String.class && "trim".equals(format)) {
                    } else {
                        asm = false;
                        break;
                    }
                }
                if ((!ASMUtils.checkName(annotation.name())) //
                        || annotation.jsonDirect() || annotation.serializeUsing() != Void.class
                        || annotation.unwrapped()
                ) {
                    asm = false;
                    break;
                }
                for (SerializerFeature feature : annotation.serialzeFeatures()) {
                    if (SerializerFeature.WriteNonStringValueAsString == feature //
                            || SerializerFeature.WriteEnumUsingToString == feature //
                            || SerializerFeature.NotWriteDefaultValue == feature
                            || SerializerFeature.BrowserCompatible == feature
                            || SerializerFeature.WriteClassName == feature) {
                        asm = false;
                        break;
                    }
                }
                if (TypeUtils.isAnnotationPresentOneToMany(method)
                        || TypeUtils.isAnnotationPresentManyToMany(method)) {
                    asm = false;
                    break;
                }
            }
        }
        if (asm) {
            try {
                ObjectSerializer asmSerializer = createASMSerializer(beanInfo);
                if (asmSerializer != null) {
                    return asmSerializer;
                }
            } catch (ClassNotFoundException ex) {
                // skip
            } catch (ClassFormatError e) {
                // skip
            } catch (ClassCastException e) {
                // skip
            } catch (OutOfMemoryError e) {
                if (e.getMessage().indexOf("Metaspace") != -1) {
                    throw e;
                }
                // skip
            } catch (Throwable e) {
                throw new JSONException("create asm serializer error, verson " + JSON.VERSION
                        + ", class " + clazz, e);
            }
        }
        return new JavaBeanSerializer(beanInfo);
    }

    public boolean isAsmEnable() {
        return asm;
    }

    public void setAsmEnable(boolean asmEnable) {
        if (ASMUtils.IS_ANDROID) {
            return;
        }
        this.asm = asmEnable;
    }

    public static SerializeConfig getGlobalInstance() {
        return globalInstance;
    }

    public SerializeConfig() {
        this(IdentityHashMap.DEFAULT_SIZE);
    }

    public SerializeConfig(boolean fieldBase) {
        this(IdentityHashMap.DEFAULT_SIZE, fieldBase);
    }

    public SerializeConfig(int tableSize) {
        this(tableSize, false);
    }

    public SerializeConfig(int tableSize,
                           boolean fieldBase) {
        this.fieldBased = fieldBase;
        serializers = new IdentityHashMap<Type, ObjectSerializer>(tableSize);
        try {
            if (asm) {
                asmFactory = new ASMSerializerFactory();
            }
        } catch (Throwable eror) {
            asm = false;
        }
        initSerializers();
    }

    private void initSerializers() {
        put(Boolean.class, BooleanCodec.instance);
        put(Character.class, CharacterCodec.instance);
        put(Byte.class, IntegerCodec.instance);
        put(Short.class, IntegerCodec.instance);
        put(Integer.class, IntegerCodec.instance);
        put(Long.class, LongCodec.instance);
        put(Float.class, FloatCodec.instance);
        put(Double.class, DoubleSerializer.instance);
        put(BigDecimal.class, BigDecimalCodec.instance);
        put(BigInteger.class, BigIntegerCodec.instance);
        put(String.class, StringCodec.instance);
        put(byte[].class, PrimitiveArraySerializer.instance);
        put(short[].class, PrimitiveArraySerializer.instance);
        put(int[].class, PrimitiveArraySerializer.instance);
        put(long[].class, PrimitiveArraySerializer.instance);
        put(float[].class, PrimitiveArraySerializer.instance);
        put(double[].class, PrimitiveArraySerializer.instance);
        put(boolean[].class, PrimitiveArraySerializer.instance);
        put(char[].class, PrimitiveArraySerializer.instance);
        put(Object[].class, ObjectArrayCodec.instance);
        put(Class.class, MiscCodec.instance);
        put(SimpleDateFormat.class, MiscCodec.instance);
        put(Currency.class, new MiscCodec());
        put(TimeZone.class, MiscCodec.instance);
        put(InetAddress.class, MiscCodec.instance);
        put(Inet4Address.class, MiscCodec.instance);
        put(Inet6Address.class, MiscCodec.instance);
        put(InetSocketAddress.class, MiscCodec.instance);
        put(File.class, MiscCodec.instance);
        put(Appendable.class, AppendableSerializer.instance);
        put(StringBuffer.class, AppendableSerializer.instance);
        put(StringBuilder.class, AppendableSerializer.instance);
        put(Charset.class, ToStringSerializer.instance);
        put(Pattern.class, ToStringSerializer.instance);
        put(Locale.class, ToStringSerializer.instance);
        put(URI.class, ToStringSerializer.instance);
        put(URL.class, ToStringSerializer.instance);
        put(UUID.class, ToStringSerializer.instance);
        // atomic
        put(AtomicBoolean.class, AtomicCodec.instance);
        put(AtomicInteger.class, AtomicCodec.instance);
        put(AtomicLong.class, AtomicCodec.instance);
        put(AtomicReference.class, ReferenceCodec.instance);
        put(AtomicIntegerArray.class, AtomicCodec.instance);
        put(AtomicLongArray.class, AtomicCodec.instance);
        put(WeakReference.class, ReferenceCodec.instance);
        put(SoftReference.class, ReferenceCodec.instance);
        put(LinkedList.class, CollectionCodec.instance);
    }

    /**
     * add class level serialize filter
     * @since 1.2.10
     */
    public void addFilter(Class<?> clazz,
                          SerializeFilter filter) {
        ObjectSerializer serializer = getObjectWriter(clazz);
        if (serializer instanceof SerializeFilterable) {
            SerializeFilterable filterable = (SerializeFilterable) serializer;
            if (this != SerializeConfig.globalInstance) {
                if (filterable == MapSerializer.instance) {
                    MapSerializer newMapSer = new MapSerializer();
                    this.put(clazz, newMapSer);
                    newMapSer.addFilter(filter);
                    return;
                }
            }
            filterable.addFilter(filter);
        }
    }

    /**
     * class level serializer feature config
     * @since 1.2.12
     */
    public void config(Class<?> clazz,
                       SerializerFeature feature,
                       boolean value) {
        ObjectSerializer serializer = getObjectWriter(clazz, false);
        if (serializer == null) {
            SerializeBeanInfo beanInfo = TypeUtils.buildBeanInfo(clazz,
                                                                 null,
                                                                 propertyNamingStrategy);
            if (value) {
                beanInfo.features |= feature.mask;
            } else {
                beanInfo.features &= ~feature.mask;
            }
            serializer = this.createJavaBeanSerializer(beanInfo);
            put(clazz, serializer);
            return;
        }
        if (serializer instanceof JavaBeanSerializer) {
            JavaBeanSerializer javaBeanSerializer = (JavaBeanSerializer) serializer;
            SerializeBeanInfo beanInfo = javaBeanSerializer.beanInfo;
            int originalFeaturs = beanInfo.features;
            if (value) {
                beanInfo.features |= feature.mask;
            } else {
                beanInfo.features &= ~feature.mask;
            }
            if (originalFeaturs == beanInfo.features) {
                return;
            }
            Class<?> serializerClass = serializer.getClass();
            if (serializerClass != JavaBeanSerializer.class) {
                ObjectSerializer newSerializer = this.createJavaBeanSerializer(beanInfo);
                this.put(clazz, newSerializer);
            }
        }
    }

    public ObjectSerializer getObjectWriter(Class<?> clazz) {
        return getObjectWriter(clazz, true);
    }

    private ObjectSerializer getObjectWriter(Class<?> clazz,
                                             boolean create) {
        ObjectSerializer writer = serializers.get(clazz);
        if (writer == null) {
            try {
                final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
                for (Object o : ServiceLoader.load(AutowiredObjectSerializer.class, classLoader)) {
                    if (!(o instanceof AutowiredObjectSerializer)) {
                        continue;
                    }
                    AutowiredObjectSerializer autowired = (AutowiredObjectSerializer) o;
                    for (Type forType : autowired.getAutowiredFor()) {
                        put(forType, autowired);
                    }
                }
            } catch (ClassCastException ex) {
                // skip
            }
            writer = serializers.get(clazz);
        }
        if (writer == null) {
            final ClassLoader classLoader = JSON.class.getClassLoader();
            if (classLoader != Thread.currentThread().getContextClassLoader()) {
                try {
                    for (Object o : ServiceLoader.load(AutowiredObjectSerializer.class,
                                                       classLoader)) {
                        if (!(o instanceof AutowiredObjectSerializer)) {
                            continue;
                        }
                        AutowiredObjectSerializer autowired = (AutowiredObjectSerializer) o;
                        for (Type forType : autowired.getAutowiredFor()) {
                            put(forType, autowired);
                        }
                    }
                } catch (ClassCastException ex) {
                    // skip
                }
                writer = serializers.get(clazz);
            }
        }
        if (writer == null) {
            String className = clazz.getName();
            Class<?> superClass;
            if (Map.class.isAssignableFrom(clazz)) {
                put(clazz, writer = MapSerializer.instance);
            } else if (List.class.isAssignableFrom(clazz)) {
                put(clazz, writer = ListSerializer.instance);
            } else if (Collection.class.isAssignableFrom(clazz)) {
                put(clazz, writer = CollectionCodec.instance);
            } else if (Date.class.isAssignableFrom(clazz)) {
                put(clazz, writer = DateCodec.instance);
            } else if (JSONAware.class.isAssignableFrom(clazz)) {
                put(clazz, writer = JSONAwareSerializer.instance);
            } else if (JSONSerializable.class.isAssignableFrom(clazz)) {
                put(clazz, writer = JSONSerializableSerializer.instance);
            } else if (JSONStreamAware.class.isAssignableFrom(clazz)) {
                put(clazz, writer = MiscCodec.instance);
            } else if (clazz.isEnum()) {
                JSONType jsonType = TypeUtils.getAnnotation(clazz, JSONType.class);
                if (jsonType != null && jsonType.serializeEnumAsJavaBean()) {
                    put(clazz, writer = createJavaBeanSerializer(clazz));
                } else {
                    put(clazz, writer = EnumSerializer.instance);
                }
            } else if ((superClass = clazz.getSuperclass()) != null && superClass.isEnum()) {
                JSONType jsonType = TypeUtils.getAnnotation(superClass, JSONType.class);
                if (jsonType != null && jsonType.serializeEnumAsJavaBean()) {
                    put(clazz, writer = createJavaBeanSerializer(clazz));
                } else {
                    put(clazz, writer = EnumSerializer.instance);
                }
            } else if (clazz.isArray()) {
                Class<?> componentType = clazz.getComponentType();
                ObjectSerializer compObjectSerializer = getObjectWriter(componentType);
                put(clazz, writer = new ArraySerializer(componentType, compObjectSerializer));
            } else if (Throwable.class.isAssignableFrom(clazz)) {
                SerializeBeanInfo beanInfo = TypeUtils.buildBeanInfo(clazz,
                                                                     null,
                                                                     propertyNamingStrategy);
                beanInfo.features |= SerializerFeature.WriteClassName.mask;
                put(clazz, writer = new JavaBeanSerializer(beanInfo));
            } else if (TimeZone.class.isAssignableFrom(clazz)
                    || Map.Entry.class.isAssignableFrom(clazz)) {
                put(clazz, writer = MiscCodec.instance);
            } else if (Appendable.class.isAssignableFrom(clazz)) {
                put(clazz, writer = AppendableSerializer.instance);
            } else if (Charset.class.isAssignableFrom(clazz)) {
                put(clazz, writer = ToStringSerializer.instance);
            } else if (Enumeration.class.isAssignableFrom(clazz)) {
                put(clazz, writer = EnumerationSerializer.instance);
            } else if (Calendar.class.isAssignableFrom(clazz) //
                    || XMLGregorianCalendar.class.isAssignableFrom(clazz)) {
                put(clazz, writer = CalendarCodec.instance);
            } else if (Clob.class.isAssignableFrom(clazz)) {
                put(clazz, writer = ClobSeriliazer.instance);
            } else if (TypeUtils.isPath(clazz)) {
                put(clazz, writer = ToStringSerializer.instance);
            } else if (Iterator.class.isAssignableFrom(clazz)) {
                put(clazz, writer = MiscCodec.instance);
            } else if (org.w3c.dom.Node.class.isAssignableFrom(clazz)) {
                put(clazz, writer = MiscCodec.instance);
            } else {
                if (className.startsWith("java.awt.") //
                        && AwtCodec.support(clazz) //
                ) {
                    // awt
                    if (!awtError) {
                        try {
                            String[] names = new String[] { "java.awt.Color",
                                                            "java.awt.Font",
                                                            "java.awt.Point",
                                                            "java.awt.Rectangle" };
                            for (String name : names) {
                                if (name.equals(className)) {
                                    put(Class.forName(name), writer = AwtCodec.instance);
                                    return writer;
                                }
                            }
                        } catch (Throwable e) {
                            awtError = true;
                            // skip
                        }
                    }
                }
                // jdk8
                if ((!jdk8Error) //
                        && (className.startsWith("java.time.") //
                                || className.startsWith("java.util.Optional") //
                                || className.equals("java.util.concurrent.atomic.LongAdder")
                                || className.equals("java.util.concurrent.atomic.DoubleAdder"))) {
                    try {
                        {
                            String[] names = new String[] { "java.time.LocalDateTime",
                                                            "java.time.LocalDate",
                                                            "java.time.LocalTime",
                                                            "java.time.ZonedDateTime",
                                                            "java.time.OffsetDateTime",
                                                            "java.time.OffsetTime",
                                                            "java.time.ZoneOffset",
                                                            "java.time.ZoneRegion",
                                                            "java.time.Period",
                                                            "java.time.Duration",
                                                            "java.time.Instant" };
                            for (String name : names) {
                                if (name.equals(className)) {
                                    put(Class.forName(name), writer = Jdk8DateCodec.instance);
                                    return writer;
                                }
                            }
                        }
                        {
                            String[] names = new String[] { "java.util.Optional",
                                                            "java.util.OptionalDouble",
                                                            "java.util.OptionalInt",
                                                            "java.util.OptionalLong" };
                            for (String name : names) {
                                if (name.equals(className)) {
                                    put(Class.forName(name), writer = OptionalCodec.instance);
                                    return writer;
                                }
                            }
                        }
                        {
                            String[] names = new String[] { "java.util.concurrent.atomic.LongAdder",
                                                            "java.util.concurrent.atomic.DoubleAdder" };
                            for (String name : names) {
                                if (name.equals(className)) {
                                    put(Class.forName(name), writer = AdderSerializer.instance);
                                    return writer;
                                }
                            }
                        }
                    } catch (Throwable e) {
                        // skip
                        jdk8Error = true;
                    }
                }
                if ((!oracleJdbcError) //
                        && className.startsWith("oracle.sql.")) {
                    try {
                        String[] names = new String[] { "oracle.sql.DATE", "oracle.sql.TIMESTAMP" };
                        for (String name : names) {
                            if (name.equals(className)) {
                                put(Class.forName(name), writer = DateCodec.instance);
                                return writer;
                            }
                        }
                    } catch (Throwable e) {
                        // skip
                        oracleJdbcError = true;
                    }
                }
                if ((!springfoxError) //
                        && className.equals("springfox.documentation.spring.web.json.Json")) {
                    try {
                        put(Class.forName("springfox.documentation.spring.web.json.Json"), //
                            writer = SwaggerJsonSerializer.instance);
                        return writer;
                    } catch (ClassNotFoundException e) {
                        // skip
                        springfoxError = true;
                    }
                }
                if ((!guavaError) //
                        && className.startsWith("com.google.common.collect.")) {
                    try {
                        String[] names = new String[] { "com.google.common.collect.HashMultimap",
                                                        "com.google.common.collect.LinkedListMultimap",
                                                        "com.google.common.collect.LinkedHashMultimap",
                                                        "com.google.common.collect.ArrayListMultimap",
                                                        "com.google.common.collect.TreeMultimap" };
                        for (String name : names) {
                            if (name.equals(className)) {
                                put(Class.forName(name), writer = GuavaCodec.instance);
                                return writer;
                            }
                        }
                    } catch (ClassNotFoundException e) {
                        // skip
                        guavaError = true;
                    }
                }
                if ((!jsonnullError) && className.equals("net.sf.json.JSONNull")) {
                    try {
                        put(Class.forName("net.sf.json.JSONNull"), writer = MiscCodec.instance);
                        return writer;
                    } catch (ClassNotFoundException e) {
                        // skip
                        jsonnullError = true;
                    }
                }
                if ((!jodaError) && className.startsWith("org.joda.")) {
                    try {
                        String[] names = new String[] { "org.joda.time.LocalDate",
                                                        "org.joda.time.LocalDateTime",
                                                        "org.joda.time.LocalTime",
                                                        "org.joda.time.Instant",
                                                        "org.joda.time.DateTime",
                                                        "org.joda.time.Period",
                                                        "org.joda.time.Duration",
                                                        "org.joda.time.DateTimeZone",
                                                        "org.joda.time.UTCDateTimeZone",
                                                        "org.joda.time.tz.CachedDateTimeZone",
                                                        "org.joda.time.tz.FixedDateTimeZone", };
                        for (String name : names) {
                            if (name.equals(className)) {
                                put(Class.forName(name), writer = JodaCodec.instance);
                                return writer;
                            }
                        }
                    } catch (ClassNotFoundException e) {
                        // skip
                        jodaError = true;
                    }
                }
                Class<?>[] interfaces = clazz.getInterfaces();
                if (interfaces.length == 1 && interfaces[0].isAnnotation()) {
                    put(clazz, AnnotationSerializer.instance);
                    return AnnotationSerializer.instance;
                }
                if (TypeUtils.isProxy(clazz)) {
                    Class<?> superClazz = clazz.getSuperclass();
                    ObjectSerializer superWriter = getObjectWriter(superClazz);
                    put(clazz, superWriter);
                    return superWriter;
                }
                if (Proxy.isProxyClass(clazz)) {
                    Class<?> handlerClass = null;
                    if (interfaces.length == 2) {
                        handlerClass = interfaces[1];
                    } else {
                        for (Class<?> proxiedInterface : interfaces) {
                            if (proxiedInterface.getName().startsWith("org.springframework.aop.")) {
                                continue;
                            }
                            if (handlerClass != null) {
                                handlerClass = null; // multi-matched
                                break;
                            }
                            handlerClass = proxiedInterface;
                        }
                    }
                    if (handlerClass != null) {
                        ObjectSerializer superWriter = getObjectWriter(handlerClass);
                        put(clazz, superWriter);
                        return superWriter;
                    }
                }
                if (create) {
                    writer = createJavaBeanSerializer(clazz);
                    put(clazz, writer);
                }
            }
            if (writer == null) {
                writer = serializers.get(clazz);
            }
        }
        return writer;
    }

    public final ObjectSerializer get(Type key) {
        return this.serializers.get(key);
    }

    public boolean put(Object type,
                       Object value) {
        return put((Type) type, (ObjectSerializer) value);
    }

    public boolean put(Type type,
                       ObjectSerializer value) {
        return this.serializers.put(type, value);
    }

    /**
     * 1.2.24
     * @param enumClasses
     */
    public void configEnumAsJavaBean(@SuppressWarnings("unchecked") Class<? extends Enum<?>>... enumClasses) {
        for (Class<? extends Enum<?>> enumClass : enumClasses) {
            put(enumClass, createJavaBeanSerializer(enumClass));
        }
    }

    /**
     * for spring config support
     * @param propertyNamingStrategy
     */
    public void setPropertyNamingStrategy(PropertyNamingStrategy propertyNamingStrategy) {
        this.propertyNamingStrategy = propertyNamingStrategy;
    }

    public void clearSerializers() {
        this.serializers.clear();
        this.initSerializers();
    }
}
