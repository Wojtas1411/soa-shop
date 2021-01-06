package com.shop.delivery;

import com.shop.delivery.types.DeliveryInfo;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Log4j2
public class DeliveryEndpoint implements Delivery{

    private final Map<Integer, DeliveryRequest> storage = new ConcurrentHashMap<>();

    private final Set<Integer> canceled = new HashSet<>();

    @Override
    public DeliveryInfo delivery(DeliveryRequest payload) throws DeliveryError {
        int id;
        if(this.storage.isEmpty() && this.canceled.isEmpty()) {
            id = 1;
        } else {
            int m1 = !this.storage.isEmpty() ? Collections.max(this.storage.keySet()) : 0;
            int m2 = !this.canceled.isEmpty() ? Collections.max(this.canceled) : 0;
            id = Math.max(m1, m2) + 1;
        }
        if(!payload.getAddress().getCountry().equals("PL")) {
            throw new DeliveryError("We cannot deliver to other countries than Poland");
        }
        storage.put(id, payload);
        DeliveryInfo result = new DeliveryInfo();
        result.setId(id);
        result.setStatus("SCHEDULED");
        result.setCost(new BigDecimal("10.0"));
        return result;
    }

    @Override
    public DeliveryInfo deliveryStatus(DeliveryStatusRequest payload) throws DeliveryError {
        DeliveryInfo result = new DeliveryInfo();
        result.setId(payload.getId());
        if(this.canceled.contains(payload.getId())) {
            result.setStatus("CANCELED");
            result.setCost(null);
            return result;
        }
        if(this.storage.containsKey(payload.getId())) {
            result.setStatus("PREPARATION");
            result.setCost(new BigDecimal("10.0"));
            return result;
        }
        throw new DeliveryError("Delivery does not exist");
    }

    @Override
    public DeliveryInfo cancelDelivery(CancelDeliveryRequest payload) throws DeliveryError {
        if(this.storage.containsKey(payload.getId())) {
            this.canceled.add(payload.getId());
            this.storage.remove(payload.getId());
            DeliveryInfo result = new DeliveryInfo();
            result.setId(payload.getId());
            result.setStatus("CANCELED");
            result.setCost(null);
            return result;
        }
        throw new DeliveryError("Delivery does not exist");
    }
}
