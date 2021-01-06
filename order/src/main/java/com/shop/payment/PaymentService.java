package com.shop.payment;

import com.shop.delivery.DeliveryInfo;
import com.shop.payment.type.Payment;
import com.shop.payment.type.PaymentError;
import com.shop.payment.type.PaymentInfo;
import com.shop.payment.type.PaymentRequest;
import com.shop.sale.type.SaleInfo;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class PaymentService {

    private final Map<String, Payment> ledger = new ConcurrentHashMap<>();
    private final Set<String> canceled = new HashSet<>();

    public PaymentInfo create(PaymentRequest paymentRequest, SaleInfo saleInfo, DeliveryInfo deliveryInfo, String orderId) throws PaymentError {
        if(this.ledger.containsKey(orderId)) {
            throw new PaymentError("Payment Error - payment already exists");
        }
        Payment payment = new Payment(
                paymentRequest.getCardNumber(),
                saleInfo.getCost() + deliveryInfo.getCost().doubleValue()
        );
        this.ledger.put(orderId, payment);
        return new PaymentInfo(
                orderId,
                "PROCESSING",
                payment.getAmount()
        );
    }

    public PaymentInfo info(String orderId) throws PaymentError {
        if(this.canceled.contains(orderId)) {
            return new PaymentInfo(orderId, "CANCELED", null);
        }
        if(!this.ledger.containsKey(orderId)) {
            throw new PaymentError("Payment Error - payment does not exist");
        }
        Payment payment = this.ledger.get(orderId);
        return new PaymentInfo(
                orderId,
                "COMPLETED",
                payment.getAmount()
        );
    }

    public PaymentInfo cancel(String orderId) throws PaymentError {
        if(!this.ledger.containsKey(orderId)) {
            throw new PaymentError("Payment Error - payment does not exist");
        }
        this.ledger.remove(orderId);
        this.canceled.add(orderId);
        return new PaymentInfo(orderId, "CANCELED", null);
    }
}
