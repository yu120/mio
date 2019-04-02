package io.mio.model;

import lombok.Data;
import lombok.ToString;

import java.io.Serializable;

@Data
@ToString
public class Response implements Serializable {

    private Object data;

}
