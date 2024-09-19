package com.example.market.shop.entity;

import com.example.market.entity.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import lombok.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class ShopItem extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    private Shop shop;
    private String name;
    private String img;
    private String description;
    private Integer price;
    private Integer stock;
    @Builder.Default
    @OneToMany(mappedBy = "item")
    private final List<ShopItemOrder> orders = new ArrayList<>();

    public void decreaseStock(Integer count) {
        if (this.stock < count) throw new ResponseStatusException(HttpStatus.CONFLICT);
        this.stock -= count;
    }
}
