package com.example.market.shop.repo;

import com.example.market.shop.entity.ShopItemOrder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ShopItemOrderRepo extends JpaRepository<ShopItemOrder, Long> {
    Page<ShopItemOrder> findAllByOrderUserId(Long userId, Pageable pageable);
    @Query("SELECT DISTINCT o " +
            "FROM ShopItemOrder o JOIN o.item i JOIN i.shop s " +
            "WHERE s.id = :shopId")
    Page<ShopItemOrder> findAllByShopId(Long shopId, Pageable pageable);
}
