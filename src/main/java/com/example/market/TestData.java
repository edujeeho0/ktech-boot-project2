package com.example.market;

import com.example.market.auth.entity.UserEntity;
import com.example.market.auth.entity.UserUpgrade;
import com.example.market.auth.repo.UserRepo;
import com.example.market.auth.repo.UserUpgradeRepo;
import com.example.market.shop.entity.Shop;
import com.example.market.shop.entity.ShopItem;
import com.example.market.shop.repo.ShopItemOrderRepo;
import com.example.market.shop.repo.ShopItemRepo;
import com.example.market.shop.repo.ShopOpenReqRepo;
import com.example.market.shop.repo.ShopRepo;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TestData {
    private final UserRepo userRepo;
    private final UserUpgradeRepo userUpgradeRepo;

    private final ShopRepo shopRepo;
    private final ShopItemRepo shopItemRepo;
    private final ShopOpenReqRepo openReqRepo;
    private final ShopItemOrderRepo orderRepo;
    private final PasswordEncoder passwordEncoder;

    public TestData(
            UserRepo userRepo,
            UserUpgradeRepo userUpgradeRepo,
            ShopRepo shopRepo,
            ShopItemRepo shopItemRepo,
            ShopOpenReqRepo openReqRepo,
            ShopItemOrderRepo orderRepo,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepo = userRepo;
        this.userUpgradeRepo = userUpgradeRepo;
        this.shopRepo = shopRepo;
        this.shopItemRepo = shopItemRepo;
        this.openReqRepo = openReqRepo;
        this.orderRepo = orderRepo;
        this.passwordEncoder = passwordEncoder;
        testUsers();
        testShops();
        testItems();
    }

    private void testUsers() {
        UserEntity normal1 = UserEntity.builder()
                .username("normal1")
                .password(passwordEncoder.encode("test"))
                .nickname("normal1")
                .name("alex")
                .age(30)
                .email("normal1@gmail.com")
                .phone("01011111111")
                .roles("ROLE_ACTIVE")
                .build();
        UserEntity normal2 = UserEntity.builder()
                .username("normal2")
                .password(passwordEncoder.encode("test"))
                .nickname("normal2")
                .name("brad")
                .age(30)
                .email("normal2@gmail.com")
                .phone("01011112222")
                .roles("ROLE_ACTIVE")
                .build();
        UserEntity normal3 = UserEntity.builder()
                .username("normal3")
                .password(passwordEncoder.encode("test"))
                .nickname("normal3")
                .name("chad")
                .age(30)
                .email("normal3@gmail.com")
                .phone("01011113333")
                .roles("ROLE_ACTIVE")
                .build();
        userRepo.saveAll(List.of(
                UserEntity.builder()
                        .username("inactive")
                        .password(passwordEncoder.encode("test"))
                        .build(),
                normal1,
                normal2,
                normal3,
                UserEntity.builder()
                        .username("owner1")
                        .password(passwordEncoder.encode("test"))
                        .nickname("owner1")
                        .name("dave")
                        .age(30)
                        .email("owner1@gmail.com")
                        .phone("01022221111")
                        .roles("ROLE_ACTIVE,ROLE_OWNER")
                        .build(),
                UserEntity.builder()
                        .username("owner2")
                        .password(passwordEncoder.encode("test"))
                        .nickname("owner2")
                        .name("eric")
                        .age(30)
                        .email("owner2@gmail.com")
                        .phone("01022222222")
                        .roles("ROLE_ACTIVE,ROLE_OWNER")
                        .build(),
                UserEntity.builder()
                        .username("owner3")
                        .password(passwordEncoder.encode("test"))
                        .nickname("owner3")
                        .name("fred")
                        .age(30)
                        .email("owner3@gmail.com")
                        .phone("01022223333")
                        .roles("ROLE_ACTIVE,ROLE_OWNER")
                        .build(),
                UserEntity.builder()
                        .username("admin")
                        .password(passwordEncoder.encode("test"))
                        .roles("ROLE_ACTIVE,ROLE_ADMIN")
                        .build()
        ));

        userUpgradeRepo.saveAll(List.of(
                UserUpgrade.builder()
                        .target(normal2)
                        .build(),
                UserUpgrade.builder()
                        .target(normal3)
                        .build()
        ));
    }

    private void testShops() {
        userRepo.findAll().stream()
                .filter(user -> user.getRoles().contains("ROLE_OWNER"))
                .forEach(user -> shopRepo.save(Shop.builder()
                        .owner(user)
                        .name(user.getUsername() + "'s shop")
                        .description("description")
                        .category(Shop.Category.DIGITAL)
//                        .status(Shop.Status.PREPARING)
                        .status(Shop.Status.OPEN)
                        .build()));

    }

    private void testItems() {
        shopRepo.findAll()
                .forEach(this::addItems);
    }

    private void addItems(Shop shop) {
        List<String> items = List.of(
                "keyboard",
                "speaker",
                "mouse",
                "monitor"
        );
        items.forEach(item -> shopItemRepo.save(ShopItem.builder()
                .shop(shop)
                .name(item + shop.getId())
                .description(shop.getName() + " " + item)
                .price((int) (shop.getId() * 10000 + item.length() * 10000))
                .stock(10)
                .build()));
    }
}
