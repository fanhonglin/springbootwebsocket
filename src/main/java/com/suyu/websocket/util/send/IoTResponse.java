package com.suyu.websocket.util.send;

import lombok.Getter;
import lombok.Setter;

/**
 * @author lazycece
 */
@Getter
@Setter
public class IoTResponse<T> {
    private Integer code;
    private String message;
    private T data;

    public boolean success() {
        return this.code == 0;
    }
}
