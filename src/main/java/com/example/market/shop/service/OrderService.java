package com.example.market.shop.service;

import com.example.market.auth.AuthenticationFacade;
import com.example.market.auth.entity.UserEntity;
import com.example.market.shop.dto.ItemOrderDto;
import com.example.market.shop.entity.ShopItem;
import com.example.market.shop.entity.ShopItemOrder;
import com.example.market.shop.repo.ShopItemOrderRepo;
import com.example.market.shop.repo.ShopItemRepo;
import jakarta.transaction.Transactional;
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
public class OrderService {
    private final AuthenticationFacade authFacade;
    private final ShopItemRepo itemRepo;
    private final ShopItemOrderRepo orderRepo;

    public ItemOrderDto createOrder(ItemOrderDto dto) {
        ShopItem item = itemRepo.findById(dto.getItemId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        UserEntity user = authFacade.extractUser();
        return ItemOrderDto.fromEntity(orderRepo.save(ShopItemOrder.builder()
                .item(item)
                .orderUser(user)
                .address(dto.getAddress())
                .count(dto.getCount())
                .totalPrice(item.getPrice() * dto.getCount())
                .build()));
    }

    public Page<ItemOrderDto> myOrders(Pageable pageable) {
        Long userId = authFacade.extractUser().getId();
        return orderRepo.findAllByOrderUserId(userId, pageable)
                .map(ItemOrderDto::fromEntity);
    }

    public ItemOrderDto readOne(Long orderId) {
        return ItemOrderDto.fromEntity(getOrder(orderId));
    }

    @Transactional
    public ItemOrderDto updateState(Long orderId, ItemOrderDto dto) {
        ShopItemOrder order = getOrder(orderId);
        if (!order.getStatus().equals(ShopItemOrder.Status.ORDERED))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        if (!dto.getStatus().equals(ShopItemOrder.Status.CANCELED))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        order.setStatus(dto.getStatus());
        return ItemOrderDto.fromEntity(orderRepo.save(order));
    }

    private ShopItemOrder getOrder(Long orderId) {
        ShopItemOrder order = orderRepo.findById(orderId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        UserEntity user = authFacade.extractUser();
        if (!order.getOrderUser().getId().equals(user.getId()))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        return order;
    }
}
