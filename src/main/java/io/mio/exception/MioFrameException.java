package io.mio.exception;

import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 * Mio Frame Exception
 *
 * @author lry
 */
@Getter
@EqualsAndHashCode(callSuper = false)
public class MioFrameException extends AbstractMioException {

    private static final int CODE_VALUE = 600;

    public MioFrameException(String message) {
        super(CODE_VALUE, message);
    }

    public MioFrameException(int code, String message) {
        super(CODE_VALUE, message);
        this.code = code;
    }

    public MioFrameException(int code, String message, Throwable cause) {
        super(code, message, cause);
    }

}
