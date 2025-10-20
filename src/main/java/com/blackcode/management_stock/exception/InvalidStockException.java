package com.blackcode.management_stock.exception;

public class InvalidStockException extends RuntimeException{
    public InvalidStockException() {
        super();
    }

    public InvalidStockException(String message) {
        super(message);
    }

    public InvalidStockException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidStockException(Throwable cause) {
        super(cause);
    }
}
