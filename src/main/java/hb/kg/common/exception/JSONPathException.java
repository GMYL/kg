package hb.kg.common.exception;

public class JSONPathException extends JSONException {
    private static final long serialVersionUID = -1683105350155293684L;

    public JSONPathException(String message) {
        super(message);
    }

    public JSONPathException(String message,
                             Throwable cause) {
        super(message, cause);
    }
}
