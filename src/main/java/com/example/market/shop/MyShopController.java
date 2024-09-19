package com.example.market.shop;


import com.example.market.shop.dto.ItemOrderDto;
import com.example.market.shop.dto.OpenRequestDto;
import com.example.market.shop.dto.ShopDto;
import com.example.market.shop.dto.ShopItemDto;
import com.example.market.shop.service.MyShopService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequestMapping("shops/my-shop")
@RequiredArgsConstructor
public class MyShopController {
    private final MyShopService myShopService;

    @GetMapping
    public ShopDto myShop() {
        return myShopService.myShop();
    }

    @GetMapping("reject-reason")
    public OpenRequestDto checkRejectReason() {
        return myShopService.rejectReason();
    }

    @PutMapping("open")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void requestOpen() {
        myShopService.requestOpen();
    }

    @PutMapping("close")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void requestClose(
            @RequestBody
            ShopDto dto
    ) {
        myShopService.requestClose(dto);
    }

    @GetMapping("orders")
    public Page<ItemOrderDto> shopOrders(
            Pageable pageable
    ) {
        return myShopService.myShopOrders(pageable);
    }

    @GetMapping("orders/{orderId}")
    public ItemOrderDto readOrder(
            @PathVariable("orderId")
            Long orderId
    ) {
        return myShopService.myShopOrder(orderId);
    }

    @PutMapping("orders/{orderId}")
    public ItemOrderDto updateOrder(
            @PathVariable("orderId")
            Long orderId,
            @RequestBody
            ItemOrderDto dto
    ) {
        return myShopService.updateOrder(orderId, dto);
    }

    @PutMapping
    public ShopDto update(
            @RequestBody
            ShopDto dto
    ) {
        return myShopService.update(dto);
    }

    @PostMapping("items")
    public ShopItemDto create(
            @RequestBody
            ShopItemDto dto
    ) {
        return myShopService.create(dto);
    }

    @GetMapping("items")
    public Page<ShopItemDto> readItems(
            Pageable pageable
    ) {
        return myShopService.readPage(pageable);
    }

    @GetMapping("items/{itemId}")
    public ShopItemDto readOne(
            @PathVariable("itemId")
            Long itemId
    ) {
        return myShopService.readOne(itemId);
    }

    @PutMapping("items/{itemId}")
    public ShopItemDto update(
            @PathVariable("itemId")
            Long itemId,
            @RequestBody
            ShopItemDto dto
    ) {
        return myShopService.update(itemId, dto);
    }

    @PutMapping(
            value = "items/{itemId}/image",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ShopItemDto updateImage(
            @PathVariable("itemId")
            Long itemId,
            @RequestParam("file")
            MultipartFile file
    ) {
        return myShopService.updateImg(itemId, file);
    }

    @DeleteMapping("items/{itemId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @PathVariable("itemId")
            Long itemId
    ) {
        myShopService.delete(itemId);
    }
}
