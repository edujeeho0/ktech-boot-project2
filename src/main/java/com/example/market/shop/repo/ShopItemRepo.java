package com.example.market.shop.repo;

import com.example.market.shop.entity.Shop;
import com.example.market.shop.entity.ShopItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShopItemRepo extends JpaRepository<ShopItem, Long> {
    Page<ShopItem> findAllByShopId(Long shopId, Pageable pageable);


    Page<ShopItem> findAllByNameContainingAndShopStatusIs(String name, Shop.Status status, Pageable pageable);
    Page<ShopItem> findAllByPriceBetweenAndShopStatusIs(Integer priceLow, Integer priceHigh, Shop.Status status, Pageable pageable);
    Page<ShopItem> findAllByNameContainingAndPriceBetweenAndShopStatusIs(String name, Integer priceLow, Integer priceHigh, Shop.Status status, Pageable pageable);
}
