package hb.kg.common.util.json.serializer;

public enum SerializerFeature {
    QuoteFieldNames,
    UseSingleQuotes,
    WriteMapNullValue,
    /**
     * 用枚举toString()值输出
     */
    WriteEnumUsingToString,
    /**
     * 用枚举name()输出
     */
    WriteEnumUsingName,
    UseISO8601DateFormat,
    /**
     * @since 1.1
     */
    WriteNullListAsEmpty,
    /**
     * @since 1.1
     */
    WriteNullStringAsEmpty,
    /**
     * @since 1.1
     */
    WriteNullNumberAsZero,
    /**
     * @since 1.1
     */
    WriteNullBooleanAsFalse,
    /**
     * @since 1.1
     */
    SkipTransientField,
    /**
     * @since 1.1
     */
    SortField,
    /**
     * @since 1.1.1
     */
    @Deprecated
    WriteTabAsSpecial,
    /**
     * @since 1.1.2
     */
    PrettyFormat,
    /**
     * @since 1.1.2
     */
    WriteClassName,
    /**
     * @since 1.1.6
     */
    DisableCircularReferenceDetect, // 32768
    /**
     * @since 1.1.9
     */
    WriteSlashAsSpecial,
    /**
     * @since 1.1.10
     */
    BrowserCompatible,
    /**
     * @since 1.1.14
     */
    WriteDateUseDateFormat,
    /**
     * @since 1.1.15
     */
    NotWriteRootClassName,
    /**
     * @since 1.1.19
     * @deprecated
     */
    DisableCheckSpecialChar,
    /**
     * @since 1.1.35
     */
    BeanToArray,
    /**
     * @since 1.1.37
     */
    WriteNonStringKeyAsString,
    /**
     * @since 1.1.42
     */
    NotWriteDefaultValue,
    /**
     * @since 1.2.6
     */
    BrowserSecure,
    /**
     * @since 1.2.7
     */
    IgnoreNonFieldGetter,
    /**
     * @since 1.2.9
     */
    WriteNonStringValueAsString,
    /**
     * @since 1.2.11
     */
    IgnoreErrorGetter,
    /**
     * @since 1.2.16
     */
    WriteBigDecimalAsPlain,
    /**
     * @since 1.2.27
     */
    MapSortField;
    SerializerFeature() {
        mask = (1 << ordinal());
    }

    public final int mask;

    public final int getMask() {
        return mask;
    }

    public static boolean isEnabled(int features,
                                    SerializerFeature feature) {
        return (features & feature.mask) != 0;
    }

    public static boolean isEnabled(int features,
                                    int fieaturesB,
                                    SerializerFeature feature) {
        int mask = feature.mask;
        return (features & mask) != 0 || (fieaturesB & mask) != 0;
    }

    public static int config(int features,
                             SerializerFeature feature,
                             boolean state) {
        if (state) {
            features |= feature.mask;
        } else {
            features &= ~feature.mask;
        }
        return features;
    }

    public static int of(SerializerFeature[] features) {
        if (features == null) {
            return 0;
        }
        int value = 0;
        for (SerializerFeature feature : features) {
            value |= feature.mask;
        }
        return value;
    }

    public final static SerializerFeature[] EMPTY = new SerializerFeature[0];
    public static final int WRITE_MAP_NULL_FEATURES = WriteMapNullValue.getMask()
            | WriteNullBooleanAsFalse.getMask() | WriteNullListAsEmpty.getMask()
            | WriteNullNumberAsZero.getMask() | WriteNullStringAsEmpty.getMask();
}
