package com.senz.sdk.exception;

import java.lang.Exception;

public class SenzException extends Exception {
    public SenzException(String detailMessage) {
        super(detailMessage);
    }
}
