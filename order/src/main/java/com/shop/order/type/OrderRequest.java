package com.shop.order.type;

import com.shop.delivery.DeliveryRequest;
import com.shop.payment.type.PaymentRequest;
import com.shop.sale.type.SaleRequest;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class OrderRequest {
    private SaleRequest saleRequest;
    private DeliveryRequest deliveryRequest;
    private PaymentRequest paymentRequest;
}
