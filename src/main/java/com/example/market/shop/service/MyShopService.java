package com.example.market.shop.service;

import com.example.market.FileHandlerUtils;
import com.example.market.auth.AuthenticationFacade;
import com.example.market.auth.entity.UserEntity;
import com.example.market.shop.dto.ItemOrderDto;
import com.example.market.shop.dto.OpenRequestDto;
import com.example.market.shop.dto.ShopDto;
import com.example.market.shop.dto.ShopItemDto;
import com.example.market.shop.entity.Shop;
import com.example.market.shop.entity.ShopItem;
import com.example.market.shop.entity.ShopItemOrder;
import com.example.market.shop.entity.ShopOpenRequest;
import com.example.market.shop.repo.ShopItemOrderRepo;
import com.example.market.shop.repo.ShopItemRepo;
import com.example.market.shop.repo.ShopOpenReqRepo;
import com.example.market.shop.repo.ShopRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@Service
@RequiredArgsConstructor
public class MyShopService {
    private final AuthenticationFacade authFacade;
    private final ShopRepo shopRepo;
    private final ShopItemRepo itemRepo;
    private final ShopOpenReqRepo openRepo;
    private final ShopItemOrderRepo orderRepo;
    private final FileHandlerUtils fileHandlerUtils;

    public ShopDto myShop() {
        UserEntity user = authFacade.extractUser();
        Shop shop = shopRepo.findByOwner(user)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        return ShopDto.fromEntity(shop, true);
    }

    @Transactional
    public ShopDto update(ShopDto dto) {
        UserEntity user = authFacade.extractUser();
        Shop shop = shopRepo.findByOwner(user)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        shop.setName(dto.getName());
        shop.setDescription(dto.getDescription());
        shop.setCategory(dto.getCategory());
        return ShopDto.fromEntity(shopRepo.save(shop));
    }

    // 쇼핑몰 개설 허가 요청
    @Transactional
    public void requestOpen() {
        UserEntity user = authFacade.extractUser();
        Shop shop = shopRepo.findByOwner(user)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (!(
                shop.getName() != null &&
                shop.getDescription() != null &&
                shop.getCategory() != null &&
                shop.getStatus() != Shop.Status.OPEN
        ))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        if (openRepo.existsByShopIdAndIsApprovedIsNull(shop.getId()))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        openRepo.save(ShopOpenRequest.builder()
                .shop(shop)
                .build());
    }

    @Transactional
    public void requestClose(ShopDto dto) {
        UserEntity user = authFacade.extractUser();
        Shop shop = shopRepo.findByOwner(user)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (shop.getStatus() != Shop.Status.OPEN)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        shop.setCloseReason(dto.getCloseReason());
        shopRepo.save(shop);
    }

    public ShopItemDto create(ShopItemDto dto) {
        UserEntity user = authFacade.extractUser();
        Shop shop = shopRepo.findByOwner(user)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR));
        return ShopItemDto.fromEntity(itemRepo.save(ShopItem.builder()
                .shop(shop)
                .name(dto.getName())
                .description(dto.getDescription())
                .price(dto.getPrice())
                .stock(dto.getStock())
                .build()));
    }

    public Page<ShopItemDto> readPage(Pageable pageable) {
        UserEntity user = authFacade.extractUser();
        Shop shop = shopRepo.findByOwner(user)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR));
        return itemRepo.findAllByShopId(shop.getId(), pageable)
                .map(ShopItemDto::fromEntity);
    }

    public ShopItemDto readOne(Long itemId) {
        ShopItem item = getItemCheckOwner(itemId);
        return ShopItemDto.fromEntity(item);
    }

    @Transactional
    public ShopItemDto update(Long itemId, ShopItemDto dto) {
        ShopItem item = getItemCheckOwner(itemId);
        item.setName(dto.getName());
        item.setDescription(dto.getDescription());
        item.setPrice(dto.getPrice());
        item.setStock(dto.getStock());
        return ShopItemDto.fromEntity(itemRepo.save(item));
    }

    @Transactional
    public void delete(Long itemId) {
        ShopItem item = getItemCheckOwner(itemId);
        itemRepo.delete(item);
    }

    private ShopItem getItemCheckOwner(Long itemId) {
        UserEntity user = authFacade.extractUser();
        Shop shop = shopRepo.findByOwner(user)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR));
        ShopItem item = itemRepo.findById(itemId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (!shop.getId().equals(item.getShop().getId()))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        return item;
    }

    public ShopItemDto updateImg(Long itemId, MultipartFile file) {
        UserEntity user = authFacade.extractUser();
        Shop shop = shopRepo.findByOwner(user)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR));
        ShopItem item = itemRepo.findById(itemId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (!shop.getId().equals(item.getShop().getId()))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        String requestPath = fileHandlerUtils.saveFile(
                String.format("shops/%d/items/%d/", shop.getId(), itemId),
                "image",
                file
        );
        item.setImg(requestPath);
        return ShopItemDto.fromEntity(itemRepo.save(item));
    }

    public OpenRequestDto rejectReason() {
        UserEntity user = authFacade.extractUser();
        Shop shop = shopRepo.findByOwner(user)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        ShopOpenRequest openRequest = openRepo.findTopByShopIdAndIsApprovedIsFalseOrderByCreatedAtDesc(shop.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST));
        return OpenRequestDto.fromEntity(openRequest);
    }

    public Page<ItemOrderDto> myShopOrders(Pageable pageable) {
        UserEntity user = authFacade.extractUser();
        Shop shop = shopRepo.findByOwner(user)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        return orderRepo.findAllByShopId(shop.getId(), pageable)
                .map(ItemOrderDto::fromEntity);
    }

    public ItemOrderDto myShopOrder(Long orderId) {
        return ItemOrderDto.fromEntity(getOrderCheckOwner(orderId));
    }

    @Transactional
    public ItemOrderDto updateOrder(Long orderId, ItemOrderDto dto) {
        ShopItemOrder order = getOrderCheckOwner(orderId);
        switch (dto.getStatus()) {
            case DECLINED -> {
                if (dto.getReason() == null)
                    throw new ResponseStatusException(HttpStatus.FORBIDDEN);
                order.setReason(dto.getReason());
                order.setStatus(dto.getStatus());
            }
            case ACCEPTED -> {
                order.setStatus(dto.getStatus());
                order.getItem().decreaseStock(order.getCount());
            }
            default -> throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        return ItemOrderDto.fromEntity(orderRepo.save(order));
    }

    public ShopItemOrder getOrderCheckOwner(Long orderId) {
        ShopItemOrder order = orderRepo.findById(orderId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        UserEntity user = authFacade.extractUser();
        ShopItem orderItem = order.getItem();
        Shop orderShop = orderItem.getShop();
        if (!orderShop.getOwner().getId().equals(user.getId()))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        if (!order.getStatus().equals(ShopItemOrder.Status.ORDERED))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        return order;
    }
}
