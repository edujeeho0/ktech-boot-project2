package com.example.market.shop.service;

import com.example.market.shop.dto.ItemSearchParams;
import com.example.market.shop.dto.ShopDto;
import com.example.market.shop.dto.ShopItemDto;
import com.example.market.shop.dto.ShopSearchParams;
import com.example.market.shop.entity.Shop;
import com.example.market.shop.repo.ShopItemRepo;
import com.example.market.shop.repo.ShopRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class SearchService {
    private final ShopRepo shopRepo;
    private final ShopItemRepo itemRepo;
    private final ShopService shopService;

    public Page<ShopDto> searchShops(ShopSearchParams params, Pageable pageable) {
        log.info(params.toString());
        if (params.getName() == null && params.getCategory() == null)
            return shopService.readPage(pageable);
        else if (params.getCategory() == null)
            return shopRepo.findAllByNameContainingAndStatusIs(params.getName(), Shop.Status.OPEN, pageable)
                    .map(ShopDto::fromEntity);
        else if (params.getName() == null)
            return shopRepo.findAllByCategoryAndStatusIs(params.getCategory(), Shop.Status.OPEN, pageable)
                    .map(ShopDto::fromEntity);
        else return shopRepo.findAllByNameContainingAndCategoryAndStatusIs(params.getName(), params.getCategory(), Shop.Status.OPEN, pageable)
                    .map(ShopDto::fromEntity);
    }

    public Page<ShopItemDto> searchItems(ItemSearchParams params, Pageable pageable) {
        if (params.getName() != null && (params.getPriceFloor() != null || params.getPriceCeil() != null)) {
            params.setNullPrice();
            return itemRepo.findAllByNameContainingAndPriceBetweenAndShopStatusIs(
                    params.getName(),
                    params.getPriceFloor(),
                    params.getPriceCeil(),
                    Shop.Status.OPEN,
                    pageable
            ).map(entity -> ShopItemDto.fromEntity(entity, true));
        }
        else if (params.getName() == null) {
            params.setNullPrice();
            return itemRepo.findAllByPriceBetweenAndShopStatusIs(params.getPriceFloor(), params.getPriceCeil(), Shop.Status.OPEN, pageable)
                    .map(entity -> ShopItemDto.fromEntity(entity, true));
        }
        else return itemRepo.findAllByNameContainingAndShopStatusIs(params.getName(), Shop.Status.OPEN, pageable)
                    .map(entity -> ShopItemDto.fromEntity(entity, true));
    }
}
