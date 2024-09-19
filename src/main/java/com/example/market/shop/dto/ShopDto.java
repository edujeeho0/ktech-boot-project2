package com.example.market.shop.dto;

import com.example.market.shop.entity.Shop;
import lombok.*;

@Getter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShopDto {
    private Long id;
    private String name;
    private String description;
    private Shop.Category category;
    private Shop.Status status;
    private String closeReason;

    public static ShopDto fromEntity(Shop entity) {
        return fromEntity(entity, false);
    }

    public static ShopDto fromEntity(Shop entity, boolean fullInfo) {
        ShopDtoBuilder builder = ShopDto.builder()
                .id(entity.getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .category(entity.getCategory())
                .status(entity.getStatus());
        if (fullInfo) builder
                .closeReason(entity.getCloseReason());
        return builder.build();
    }
}
