package com.example.market.admin;

import com.example.market.admin.dto.UserUpgradeDto;
import com.example.market.auth.dto.UserDto;
import com.example.market.auth.entity.UserUpgrade;
import com.example.market.auth.repo.UserRepo;
import com.example.market.auth.repo.UserUpgradeRepo;
import com.example.market.shop.dto.OpenRequestDto;
import com.example.market.shop.dto.ShopDto;
import com.example.market.shop.entity.Shop;
import com.example.market.shop.entity.ShopOpenRequest;
import com.example.market.shop.repo.ShopOpenReqRepo;
import com.example.market.shop.repo.ShopRepo;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class AdminService {
    private final UserRepo userRepo;
    private final UserUpgradeRepo userUpgradeRepo;
    private final ShopRepo shopRepo;
    private final ShopOpenReqRepo openRepo;


    public Page<UserUpgradeDto> listRequests(Pageable pageable) {
        return userUpgradeRepo.findAll(pageable)
                .map(UserUpgradeDto::fromEntity);
    }

    @Transactional
    public UserUpgradeDto approveUpgrade(Long id) {
        UserUpgrade upgrade = userUpgradeRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        upgrade.setApproved(true);
        if (!upgrade.getTarget().getRoles().contains("ROLE_OWNER")) {
            shopRepo.save(Shop.builder()
                    .owner(upgrade.getTarget())
                    .build());
            upgrade.getTarget().setRoles("ROLE_ACTIVE,ROLE_OWNER");
        }
        return UserUpgradeDto.fromEntity(userUpgradeRepo.save(upgrade));
    }

    @Transactional
    public UserUpgradeDto disapproveUpgrade(Long id) {
        UserUpgrade upgrade = userUpgradeRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        upgrade.setApproved(false);
        return UserUpgradeDto.fromEntity(upgrade);
    }

    public Page<UserDto> readUsersPage(Pageable pageable) {
        return userRepo.findAll(pageable)
                .map(UserDto::fromEntity);
    }

    public Page<ShopDto> readOpenRequests(Pageable pageable) {
        return shopRepo.findOpenRequested(pageable)
                .map(ShopDto::fromEntity);
    }

    public Page<ShopDto> readCloseRequests(Pageable pageable) {
        return shopRepo.findCloseRequested(Shop.Status.CLOSED, pageable)
                .map(shop -> ShopDto.fromEntity(shop, true));
    }

    public void approveOpen(Long shopId) {
        ShopOpenRequest openRequest = openRepo.findTopByShopIdAndIsApprovedIsNullOrderByCreatedAtDesc(shopId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST));
        openRequest.setIsApproved(true);
        openRequest.getShop().setStatus(Shop.Status.OPEN);
        openRepo.save(openRequest);
    }

    public void disapproveOpen(Long shopId, OpenRequestDto dto) {
        ShopOpenRequest openRequest = openRepo.findTopByShopIdAndIsApprovedIsNullOrderByCreatedAtDesc(shopId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST));
        openRequest.setIsApproved(false);
        openRequest.setReason(dto.getReason());
        openRequest.getShop().setStatus(Shop.Status.REJECTED);
        openRepo.save(openRequest);
    }

    public void approveClose(Long shopId) {
        Shop shop = shopRepo.findById(shopId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (shop.getCloseReason() == null || shop.getStatus() != Shop.Status.OPEN)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);

        shop.setStatus(Shop.Status.CLOSED);
        shopRepo.save(shop);
    }
}
