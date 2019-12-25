package io.mio.filter;

import lombok.Data;

import java.io.Serializable;
import java.util.Map;

/**
 * MioResponse
 *
 * @author lry
 */
@Data
public class MioResponse implements Serializable {

    private Map<String, Object> headers;
    private byte[] data;

}
