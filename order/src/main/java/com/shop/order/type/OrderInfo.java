package com.shop.order.type;

import com.shop.delivery.DeliveryInfo;
import com.shop.payment.type.PaymentInfo;
import com.shop.sale.type.SaleInfo;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class OrderInfo {
    private String id;

    private String status = "OK";

    private DeliveryInfo deliveryInfo;
    private PaymentInfo paymentInfo;
    private SaleInfo saleInfo;

    public OrderInfo(String id) {
        this.id = id;
    }
}
