package com.example.market.shop.dto;

import com.example.market.shop.entity.ShopItem;
import lombok.*;

@Getter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShopItemDto {
    private Long id;
    private String name;
    private String description;
    private String img;
    private Integer price;
    private Integer stock;
    private ShopDto shop;

    public static ShopItemDto fromEntity(ShopItem entity) {
        return fromEntity(entity, false);
    }

    public static ShopItemDto fromEntity(ShopItem entity, boolean withShop) {
        return ShopItemDto.builder()
                .id(entity.getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .img(entity.getImg())
                .price(entity.getPrice())
                .stock(entity.getStock())
                .shop(withShop ? ShopDto.fromEntity(entity.getShop()) : null)
                .build();
    }
}
