package io.mio.exception;

import lombok.Getter;

/**
 * Abstract Mio Exception
 *
 * @author lry
 */
@Getter
public abstract class AbstractMioException extends RuntimeException {

    protected int code;
    protected String message;

    AbstractMioException(int code, String message) {
        this(code, message, null);
    }

    AbstractMioException(int code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }

}
