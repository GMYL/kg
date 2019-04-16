package hb.kg.common.util.json;

import java.io.IOException;

/**
 * Beans that support customized output of JSON text to a writer shall implement
 * this interface.
 */
public interface JSONStreamAware {
    /**
     * write JSON string to out.
     */
    void writeJSONString(Appendable out) throws IOException;
}
