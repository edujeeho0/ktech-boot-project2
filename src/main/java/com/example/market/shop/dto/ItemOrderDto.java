package com.example.market.shop.dto;

import com.example.market.shop.entity.ShopItemOrder;
import lombok.*;

@Getter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class ItemOrderDto {
    private Long id;
    private Long itemId;
    private String itemName;
    private String address;
    private Integer count;
    private Integer totalPrice;
    private ShopItemOrder.Status status;
    private String reason;

    public static ItemOrderDto fromEntity(ShopItemOrder entity) {
        return ItemOrderDto.builder()
                .id(entity.getId())
                .itemId(entity.getItem().getId())
                .itemName(entity.getItem().getName())
                .address(entity.getAddress())
                .count(entity.getCount())
                .totalPrice(entity.getTotalPrice())
                .status(entity.getStatus())
                .reason(entity.getReason())
                .build();
    }
}
