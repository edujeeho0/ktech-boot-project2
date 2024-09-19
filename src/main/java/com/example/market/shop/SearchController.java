package com.example.market.shop;

import com.example.market.shop.dto.ItemSearchParams;
import com.example.market.shop.dto.ShopDto;
import com.example.market.shop.dto.ShopItemDto;
import com.example.market.shop.dto.ShopSearchParams;
import com.example.market.shop.service.SearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("search")
@RequiredArgsConstructor
public class SearchController {
    private final SearchService service;
    @GetMapping("shops")
    public Page<ShopDto> searchShops(
            ShopSearchParams params,
            Pageable pageable
    ) {
        return service.searchShops(params, pageable);
    }

    @GetMapping("items")
    public Page<ShopItemDto> searchItems(
            ItemSearchParams params,
            Pageable pageable
    ) {
        return service.searchItems(params, pageable);
    }
}
