package io.mio.core.commons;

import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 * MioException
 *
 * @author lry
 */
@Getter
@EqualsAndHashCode(callSuper = false)
public class MioException extends RuntimeException {

    public static final int CHANNEL_NULL = 1;
    public static final int CONTENT_OUT_LIMIT = 2;
    public static final int WAIT_INTERRUPTED = 3;
    public static final int FUTURE_CANCEL = 4;
    public static final int FUTURE_TIMEOUT_CANCEL = 5;
    public static final int FUTURE_LISTENER_NULL = 6;
    public static final int NOT_FOUND_CLIENT = 7;

    private final int code;
    private final String message;
    private final Object data;
    private final Throwable cause;

    public MioException(int code, String message) {
        this(code, message, null, null);
    }

    public MioException(int code, String message, Object data) {
        this(code, message, data, null);
    }

    public MioException(int code, String message, Throwable cause) {
        this(code, message, null, cause);
    }

    public MioException(int code, String message, Object data, Throwable cause) {
        super(message, cause);
        this.code = code;
        this.message = message;
        this.data = data;
        this.cause = cause;
    }

}
