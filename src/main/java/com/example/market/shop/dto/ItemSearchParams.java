package com.example.market.shop.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class ItemSearchParams {
    private String name;
    private Integer priceFloor;
    private Integer priceCeil;

    public void setNullPrice() {
        priceFloor = priceFloor == null ? 0 : priceFloor;
        priceCeil = priceCeil == null ? Integer.MAX_VALUE : priceCeil;
    }
}
