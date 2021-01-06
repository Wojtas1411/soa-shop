package com.shop.payment.type;

public class PaymentError extends Exception{
    public PaymentError(String message) {
        super(message);
    }
}
