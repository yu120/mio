package io.mio.rpc.filter;

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
    private Object data;

}