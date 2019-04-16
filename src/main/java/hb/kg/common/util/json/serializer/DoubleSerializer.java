package hb.kg.common.util.json.serializer;

import java.io.IOException;
import java.lang.reflect.Type;
import java.text.DecimalFormat;

public class DoubleSerializer implements ObjectSerializer {
    public final static DoubleSerializer instance = new DoubleSerializer();
    private DecimalFormat decimalFormat = null;

    public DoubleSerializer() {}

    public DoubleSerializer(DecimalFormat decimalFormat) {
        this.decimalFormat = decimalFormat;
    }

    public DoubleSerializer(String decimalFormat) {
        this(new DecimalFormat(decimalFormat));
    }

    public void write(JSONSerializer serializer,
                      Object object,
                      Object fieldName,
                      Type fieldType,
                      int features)
            throws IOException {
        SerializeWriter out = serializer.out;
        if (object == null) {
            out.writeNull(SerializerFeature.WriteNullNumberAsZero);
            return;
        }
        double doubleValue = ((Double) object).doubleValue();
        if (Double.isNaN(doubleValue) //
                || Double.isInfinite(doubleValue)) {
            out.writeNull();
        } else {
            if (decimalFormat == null) {
                out.writeDouble(doubleValue, true);
            } else {
                String doubleText = decimalFormat.format(doubleValue);
                out.write(doubleText);
            }
        }
    }
}
