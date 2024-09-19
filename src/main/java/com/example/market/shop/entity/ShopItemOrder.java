package com.example.market.shop.entity;

import com.example.market.auth.entity.UserEntity;
import com.example.market.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class ShopItemOrder extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private ShopItem item;
    @ManyToOne(fetch = FetchType.LAZY)
    private UserEntity orderUser;
    private String address;
    private Integer count;
    private Integer totalPrice;
    @Setter
    @Builder.Default
    @Enumerated(EnumType.STRING)
    private Status status = Status.ORDERED;
    @Setter
    private String reason;

    public enum Status {
        ORDERED, ACCEPTED, DECLINED, CANCELED
    }
}
