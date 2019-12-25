package io.mio.filter;

import lombok.Data;

import java.io.Serializable;
import java.util.Map;

/**
 * MioRequest
 *
 * @author lry
 */
@Data
public class MioRequest implements Serializable {

    private Map<String, Object> headers;
    private byte[] header;
    private byte[] data;

}
