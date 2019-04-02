package io.mio.model;

import lombok.Data;
import lombok.ToString;

import java.io.Serializable;

@Data
@ToString
public class Request implements Serializable {

    private String module;

}
