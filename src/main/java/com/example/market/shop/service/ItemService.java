package com.example.market.shop.service;

import com.example.market.auth.AuthenticationFacade;
import com.example.market.shop.dto.ShopItemDto;
import com.example.market.shop.entity.Shop;
import com.example.market.shop.entity.ShopItem;
import com.example.market.shop.repo.ShopItemRepo;
import com.example.market.shop.repo.ShopRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@Service
@RequiredArgsConstructor
public class ItemService {
    private final AuthenticationFacade authFacade;
    private final ShopRepo shopRepo;
    private final ShopItemRepo itemRepo;

    public Page<ShopItemDto> readPage(
            Long shopId,
            Pageable pageable
    ) {
        checkShopStatus(shopId);
        return itemRepo.findAllByShopId(shopId, pageable)
                .map(ShopItemDto::fromEntity);
    }

    public ShopItemDto readOne(
            Long shopId,
            Long itemId
    ) {
        checkShopStatus(shopId);
        ShopItem item = itemRepo.findById(itemId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (!item.getShop().getId().equals(shopId))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        return ShopItemDto.fromEntity(item);
    }

    private void checkShopStatus(Long shopId) {
        Shop shop = shopRepo.findById(shopId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (!shop.getStatus().equals(Shop.Status.OPEN))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
    }
}
