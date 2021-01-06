package com.shop.order;

import com.shop.delivery.DeliveryInfo;
import com.shop.order.type.OrderError;
import com.shop.order.type.OrderInfo;
import com.shop.order.type.OrderRequest;
import com.shop.payment.type.PaymentInfo;
import com.shop.sale.type.SaleInfo;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class OrderService {

    private final Map<String, OrderInfo> orderInfos = new ConcurrentHashMap<>();

    private final Map<String, OrderRequest> orderRequests = new ConcurrentHashMap<>();

    public OrderInfo create(String orderId, OrderRequest orderRequest) throws OrderError {
        if(this.orderInfos.containsKey(orderId)) {
            throw new OrderError("Order error - order id already exists");
        }
        OrderInfo oi = new OrderInfo(orderId);
        this.orderInfos.put(orderId, oi);
        this.orderRequests.put(orderId, orderRequest);
        return oi;
    }

    public OrderInfo update(String orderId, SaleInfo saleInfo) throws OrderError {
        checkIfMissing(orderId);
        this.orderInfos.get(orderId).setSaleInfo(saleInfo);
        return this.orderInfos.get(orderId);
    }

    public OrderInfo update(String orderId, DeliveryInfo deliveryInfo) throws OrderError {
        checkIfMissing(orderId);
        this.orderInfos.get(orderId).setDeliveryInfo(deliveryInfo);
        return this.orderInfos.get(orderId);
    }

    public OrderInfo update(String orderId, PaymentInfo paymentInfo) throws OrderError {
        checkIfMissing(orderId);
        this.orderInfos.get(orderId).setPaymentInfo(paymentInfo);
        return this.orderInfos.get(orderId);
    }

    public OrderInfo get(String orderId) throws OrderError {
        checkIfMissing(orderId);
        return this.orderInfos.get(orderId);
    }

    public OrderRequest getRequest(String orderId) throws OrderError {
        checkIfMissing(orderId);
        return this.orderRequests.get(orderId);
    }

    public OrderInfo cancel(String orderId) throws OrderError {
        checkIfMissing(orderId);
        this.orderInfos.remove(orderId);
        return new OrderInfo(orderId);
    }

    private void checkIfMissing(String orderId) throws OrderError {
        if(!this.orderInfos.containsKey(orderId)) {
            throw new OrderError("Order error - order does not exist - " + orderId);
        }
    }
}
