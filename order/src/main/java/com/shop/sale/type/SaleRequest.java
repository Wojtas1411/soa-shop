package com.shop.sale.type;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SaleRequest {

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ProductEntry {
        String productName;
        Double price;
    }

    private List<ProductEntry> products = new ArrayList<>();

    public double calculateTotal() {
        List<Double> prices =  this.products.stream().map(ProductEntry::getPrice).collect(Collectors.toList());
        return prices.stream().reduce(0.0, Double::sum);
    }
}
