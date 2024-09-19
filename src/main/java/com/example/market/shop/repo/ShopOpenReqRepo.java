package com.example.market.shop.repo;

import com.example.market.shop.entity.Shop;
import com.example.market.shop.entity.ShopOpenRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ShopOpenReqRepo extends JpaRepository<ShopOpenRequest, Long> {
    Boolean existsByShopIdAndIsApprovedIsNull(Long shopId);
    Optional<ShopOpenRequest> findTopByShopIdAndIsApprovedIsNullOrderByCreatedAtDesc(Long shopId);
    Optional<ShopOpenRequest> findTopByShopIdAndIsApprovedIsFalseOrderByCreatedAtDesc(Long shopId);
}
