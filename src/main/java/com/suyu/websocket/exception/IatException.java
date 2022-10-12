package com.suyu.websocket.exception;

import java.io.Serializable;

public class IatException extends Exception implements Serializable {
    private static final long serialVersionUID = 5018770229232677878L;

    private int errorCode = 1000;

    public IatException() {
    }

    public IatException(int errorCode, String arg0) {
        super(arg0);
        this.errorCode = errorCode;
    }

    public IatException(String arg0) {
        super(arg0);
    }

    public IatException(String arg0, Throwable arg1) {
        super(arg0, arg1);
    }

    public IatException(int errorCode, String arg0, Throwable arg1) {
        super(arg0, arg1);
        this.errorCode = errorCode;
    }

    public IatException(Throwable arg0) {
        super(arg0);
    }

    public int getErrorCode() {
        return this.errorCode;
    }

    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }
}
