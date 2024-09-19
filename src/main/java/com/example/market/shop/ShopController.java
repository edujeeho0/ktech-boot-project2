package com.example.market.shop;

import com.example.market.shop.dto.ShopDto;
import com.example.market.shop.service.ShopService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("shops")
@RequiredArgsConstructor
public class ShopController {
    private final ShopService service;

    @GetMapping
    public Page<ShopDto> readPage(
            Pageable pageable
    ) {
        return service.readPage(pageable);
    }


    @GetMapping("{id}")
    public ShopDto readOne(
            @PathVariable("id")
            Long id
    ) {
        return service.readOne(id);
    }
}
