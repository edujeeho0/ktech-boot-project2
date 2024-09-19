package com.example.market.shop.dto;

import com.example.market.shop.entity.ShopOpenRequest;
import lombok.*;

@Getter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class OpenRequestDto {
    private Long id;
    private Long shopId;
    private String shopName;
    private Boolean isApproved;
    private String reason;

    public static OpenRequestDto fromEntity(ShopOpenRequest entity) {
        return OpenRequestDto.builder()
                .id(entity.getId())
                .shopId(entity.getShop().getId())
                .shopName(entity.getShop().getName())
                .isApproved(entity.getIsApproved())
                .reason(entity.getReason())
                .build();
    }
}
