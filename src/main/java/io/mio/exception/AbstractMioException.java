package io.mio.exception;

import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 * Abstract Mio Exception
 *
 * @author lry
 */
@Getter
@EqualsAndHashCode(callSuper = false)
public abstract class AbstractMioException extends RuntimeException {

    protected int code;
    protected String message;

    public AbstractMioException(int code, String message) {
        this(code, message, null);
    }

    public AbstractMioException(int code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }

}
