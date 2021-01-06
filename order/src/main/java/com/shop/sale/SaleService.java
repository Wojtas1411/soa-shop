package com.shop.sale;

import com.shop.sale.type.SaleError;
import com.shop.sale.type.SaleInfo;
import com.shop.sale.type.SaleRequest;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class SaleService {

    private final Map<String, SaleRequest> sales = new ConcurrentHashMap<>();

    private final Set<String> canceled = new HashSet<>();

    public SaleInfo create(SaleRequest saleRequest, String orderId) throws SaleError {
        if (this.sales.containsKey(orderId)) {
            throw new SaleError("Sales Error - Order already exists");
        }
        if(saleRequest.getProducts().isEmpty()) {
            this.canceled.add(orderId);
            throw new SaleError("Sale Error - empty list");
        }
        sales.put(orderId, saleRequest);
        return new SaleInfo(orderId, "SCHEDULED", saleRequest.calculateTotal());
    }

    public SaleInfo info(String orderId) throws SaleError {
        if(this.canceled.contains(orderId)) {
            return new SaleInfo(orderId, "CANCELED", null);
        }
        if (!sales.containsKey(orderId)) {
            throw new SaleError("Sales Error - Order does not exist");
        }
        SaleRequest saleRequest = this.sales.get(orderId);
        return new SaleInfo(
                orderId,
                "PROCESSING",
                saleRequest.calculateTotal()
        );
    }

    public SaleInfo cancel(String orderId) throws SaleError {
        if (!sales.containsKey(orderId)) {
            throw new SaleError("Sales Error - Order does not exist");
        }
        this.sales.remove(orderId);
        this.canceled.add(orderId);
        return new SaleInfo(orderId, "CANCELED", null);
    }


}
