package com.example.market.shop.service;

import com.example.market.auth.AuthenticationFacade;
import com.example.market.auth.entity.UserEntity;
import com.example.market.shop.dto.ShopDto;
import com.example.market.shop.entity.Shop;
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
public class ShopService {
    private final AuthenticationFacade authFacade;
    private final ShopRepo shopRepo;

    public ShopDto readOne(Long id) {
        Shop shop = shopRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        UserEntity user = authFacade.extractUser();
        if (
                shop.getStatus() != Shop.Status.OPEN &&
                !shop.getOwner().getId().equals(user.getId()))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        return ShopDto.fromEntity(shop);
    }

    public Page<ShopDto> readPage(Pageable pageable) {
        return shopRepo.findAllByStatus(Shop.Status.OPEN, pageable)
                .map(ShopDto::fromEntity);
    }
}
