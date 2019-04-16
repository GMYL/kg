package hb.kg.common.exception;

/**
 * 对移植的fastjson的异常抛出
 */
public class JSONException extends RuntimeException {
    private static final long serialVersionUID = 2871213024004894135L;

    public JSONException() {
        super();
    }

    public JSONException(String message) {
        super(message);
    }

    public JSONException(String message,
                         Throwable cause) {
        super(message, cause);
    }
}
