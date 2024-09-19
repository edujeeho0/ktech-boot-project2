package com.example.market.shop;

import com.example.market.shop.dto.ItemOrderDto;
import com.example.market.shop.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("orders")
@RequiredArgsConstructor
public class OrderController {
    private final OrderService service;

    @PostMapping
    public ItemOrderDto create(
            @RequestBody
            ItemOrderDto dto
    ) {
        return service.createOrder(dto);
    }

    @GetMapping
    public Page<ItemOrderDto> myOrders(
            Pageable pageable
    ) {
        return service.myOrders(pageable);
    }

    @GetMapping("{id}")
    public ItemOrderDto readOne(
            @PathVariable("id")
            Long id
    ) {
        return service.readOne(id);
    }

    @PutMapping("{id}")
    public ItemOrderDto updateState(
            @PathVariable("id")
            Long id,
            @RequestBody
            ItemOrderDto dto
    ) {
        return service.updateState(id, dto);
    }

}
