package com.shop.payment.type;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class Payment {
    private final String cardNumber;
    private final Double amount;
}
