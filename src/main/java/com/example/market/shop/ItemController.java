package com.example.market.shop;

import com.example.market.shop.dto.ShopItemDto;
import com.example.market.shop.service.ItemService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("shops/{shopId}/items")
@RequiredArgsConstructor
public class ItemController {
    private final ItemService service;
    @GetMapping
    public Page<ShopItemDto> readPage(
            @PathVariable("shopId")
            Long shopId,
            Pageable pageable
    ) {
        return service.readPage(shopId, pageable);
    }

    @GetMapping("{itemId}")
    public ShopItemDto readOne(
            @PathVariable("shopId")
            Long shopId,
            @PathVariable("itemId")
            Long itemId
    ) {
        return service.readOne(shopId, itemId);
    }
}
