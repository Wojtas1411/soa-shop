package com.shop.order.type;

public class OrderError extends Exception{
    public OrderError(String message) {
        super(message);
    }
}
