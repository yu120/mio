package io.mio.rpc.filter;

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

    private String group;
    private String service;
    private String method;

    private Map<String, Object> headers;
    private Object data;

}
