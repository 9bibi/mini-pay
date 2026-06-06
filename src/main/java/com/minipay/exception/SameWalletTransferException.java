package com.minipay.exception;

public class SameWalletTransferException extends RuntimeException {

    public SameWalletTransferException(String message) {
        super(message);
    }
}
