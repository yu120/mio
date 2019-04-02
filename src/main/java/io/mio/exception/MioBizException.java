package io.mio.exception;

import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 * Mio Biz Exception
 *
 * @author lry
 */
@Getter
@EqualsAndHashCode(callSuper = false)
public class MioBizException extends AbstractMioException {

    private static final int CODE_VALUE = 500;

    public MioBizException(String message) {
        super(CODE_VALUE, message);
    }

    public MioBizException(String message, Throwable cause) {
        super(CODE_VALUE, message, cause);
    }

    public MioBizException(int code, String message) {
        super(code, message);
    }

    public MioBizException(int code, String message, Throwable cause) {
        super(code, message, cause);
    }

}
