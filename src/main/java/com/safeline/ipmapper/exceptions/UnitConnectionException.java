package com.safeline.ipmapper.exceptions;

public class UnitConnectionException extends Exception{

    private int status;

    public UnitConnectionException(int status) {
        super("Connection refused, status code:" + status);
        this.status = status;
    }

    public int getStatus() { return status; }
}
