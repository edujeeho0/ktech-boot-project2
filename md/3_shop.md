# 쇼핑몰 운영하기

## 쇼핑몰 개설 & 폐쇄

사업자 사용자는 자신의 쇼핑몰을 개설해서 운영할 수 있다. 이때 쇼핑몰 자체는
사업자 사용자로 업그레이드 되면서 자동으로 생성되며, 이후 주인이 정보와 상품을 추가한 뒤
관리자에게 개설 신청을 할 수 있다.

```java
@Service
@RequiredArgsConstructor
public class AdminService {
    private final UserRepo userRepo;
    private final UserUpgradeRepo userUpgradeRepo;
    private final ShopRepo shopRepo;
    private final ShopOpenReqRepo openRepo;

    // ...
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
}
```

개설 신청은 따로 `Entity`를 준비하지만, 폐쇄 신청은 허가할 수 밖에 없기 때문에
따로 `Entity`를 만들지 않고 `Shop`에 폐쇄 이유를 추가한다.

```java
@Entity
public class Shop extends BaseEntity {
    @OneToOne(fetch = FetchType.LAZY)
    private UserEntity owner;
    private String name;
    private String description;
    @Enumerated(EnumType.STRING)
    private Category category;
    @Enumerated(EnumType.STRING)
    private Status status = Status.PREPARING;
    private String closeReason;

    @OneToMany(mappedBy = "shop", fetch = FetchType.LAZY)
    private final List<ShopItem> items = new ArrayList<>();
    
    public enum Category {
        FOOD, FASHION, DIGITAL, SPORTS, FURNISHING
    }

    public enum Status {
        PREPARING, REJECTED, OPEN, CLOSED
    }
}
```

대신 이미 폐쇄된 쇼핑몰도 다시 개설 신청을 할 수 있다. 이 경우 이미 있는 쇼핑몰의 상태가 다시 `OPEN`이 된다.

## 쇼핑몰 관리

쇼핑몰 관리를 위한 URL을 `/shops/my-shop` 아래 두고, 요청할때 `OWNER` Role이 있어야 요청할 수 있도록 조정한다.
그리고 각 기능을 활용할 때 사용자 정보를 바탕으로 쇼핑몰을 조회한다.

```java
// 내 쇼핑몰 조회하기
public ShopDto myShop() {
    UserEntity user = authFacade.extractUser();
    Shop shop = shopRepo.findByOwner(user)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    return ShopDto.fromEntity(shop, true);
}

// 내 쇼핑몰 갱신하기
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
```

만약 쇼핑몰 개설이 반려가 되었다면 그 이유를 확인할 수 있는 기능도 추가했다.

```java
public OpenRequestDto rejectReason() {
    UserEntity user = authFacade.extractUser();
    Shop shop = shopRepo.findByOwner(user)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    ShopOpenRequest openRequest = openRepo.findTopByShopIdAndIsApprovedIsFalseOrderByCreatedAtDesc(shop.getId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST));
    return OpenRequestDto.fromEntity(openRequest);
}
```

상품은 사업자 사용자가 CRUD가 가능하다.
이때, 제출한 `itemId`에 해당하는 상품이 사용자의 쇼핑몰의 상품인지를 확인하는 과정이 필요하다.
이를 별도의 메서드로 분리했다.

```java
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
```

또한 자신의 쇼핑몰의 주문들을 조회하는 기능도 추가한다.

```java
public Page<ItemOrderDto> myShopOrders(Pageable pageable) {
    UserEntity user = authFacade.extractUser();
    Shop shop = shopRepo.findByOwner(user)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    return orderRepo.findAllByShopId(shop.getId(), pageable)
            .map(ItemOrderDto::fromEntity);
}
```

## 쇼핑몰 조회 / 검색

쇼핑몰의 조회는 특정 조건이 붙어있는데, 이를 위해 간단한 JPQL을 작성하였다.
`Shop`을 조회하되 `Shop.items.orders` 중 가장 나중에 만들어진 `Order`를 기준으로 정렬한다.

```java
public interface ShopRepo extends JpaRepository<Shop, Long> {
    @Query("SELECT DISTINCT s " +
            "FROM Shop s JOIN s.items i JOIN i.orders o " +
            "WHERE s.status = :status " +
            "ORDER BY o.createdAt DESC ")
    Page<Shop> findAllByStatus(Shop.Status status, Pageable pageable);
}
```

검색은 쇼핑몰 이름, 분류를 기준으로 검색할 수 있다.
이들은 `ShopRepo`의 Query Method를 사용해 `SearchService`에서 조회한다. 

```java
// ShopRepo
Page<Shop> findAllByNameContainingAndStatusIs(String name, Shop.Status status, Pageable pageable);
Page<Shop> findAllByCategoryAndStatusIs(Shop.Category category, Shop.Status status, Pageable pageable);
Page<Shop> findAllByNameContainingAndCategoryAndStatusIs(String name, Shop.Category category, Shop.Status status, Pageable pageable);

// SearchService
public Page<ShopDto> searchShops(ShopSearchParams params, Pageable pageable) {
    log.info(params.toString());
    if (params.getName() == null && params.getCategory() == null)
        return shopService.readPage(pageable);
    else if (params.getCategory() == null)
        return shopRepo.findAllByNameContainingAndStatusIs(params.getName(), Shop.Status.OPEN, pageable)
                .map(ShopDto::fromEntity);
    else if (params.getName() == null)
        return shopRepo.findAllByCategoryAndStatusIs(params.getCategory(), Shop.Status.OPEN, pageable)
                .map(ShopDto::fromEntity);
    else return shopRepo.findAllByNameContainingAndCategoryAndStatusIs(params.getName(), params.getCategory(), Shop.Status.OPEN, pageable)
                .map(ShopDto::fromEntity);
}
```

## 상품 조회 / 검색

쇼핑몰에 방문하면 상품을 조회할 수 있으며, 개별 조회도 가능하다.
이때, 쇼핑몰이 개설된 상태여야 상품을 조회할 수 있기 때문에, 쇼핑몰의 상태를 조회하는 메서드를 추가했다.

```java
public Page<ShopItemDto> readPage(
        Long shopId,
        Pageable pageable
) {
    checkShopStatus(shopId);
    return itemRepo.findAllByShopId(shopId, pageable)
            .map(ShopItemDto::fromEntity);
}

public ShopItemDto readOne(
        Long shopId,
        Long itemId
) {
    checkShopStatus(shopId);
    ShopItem item = itemRepo.findById(itemId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    if (!item.getShop().getId().equals(shopId))
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
    return ShopItemDto.fromEntity(item);
}

private void checkShopStatus(Long shopId) {
    Shop shop = shopRepo.findById(shopId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    UserEntity user = authFacade.extractUser();
    if (
            !shop.getStatus().equals(Shop.Status.OPEN) &&
            !shop.getOwner().getId().equals(user.getId())
    )
        throw new ResponseStatusException(HttpStatus.FORBIDDEN);
}
```

상품 검색의 경우 쇼핑몰 검색과 동일하게 진행하는데,
최소 또는 최대 가격 범위가 주어지지 않았을 때 해당 값을 0 또는 최댓값으로 설정하는 메서드를 추가했다.

```java
public class ItemSearchParams {
    private String name;
    private Integer priceFloor;
    private Integer priceCeil;

    public void setNullPrice() {
        priceFloor = priceFloor == null ? 0 : priceFloor;
        priceCeil = priceCeil == null ? Integer.MAX_VALUE : priceCeil;
    }
}

// ShopItemRepo
Page<ShopItem> findAllByNameContainingAndShopStatusIs(String name, Shop.Status status, Pageable pageable);
Page<ShopItem> findAllByPriceBetweenAndShopStatusIs(Integer priceLow, Integer priceHigh, Shop.Status status, Pageable pageable);
Page<ShopItem> findAllByNameContainingAndPriceBetweenAndShopStatusIs(String name, Integer priceLow, Integer priceHigh, Shop.Status status, Pageable pageable);

// SearchService
public Page<ShopItemDto> searchItems(ItemSearchParams params, Pageable pageable) {
    if (params.getName() != null && (params.getPriceFloor() != null || params.getPriceCeil() != null)) {
        params.setNullPrice();
        return itemRepo.findAllByNameContainingAndPriceBetweenAndShopStatusIs(
                params.getName(),
                params.getPriceFloor(),
                params.getPriceCeil(),
                Shop.Status.OPEN,
                pageable
        ).map(entity -> ShopItemDto.fromEntity(entity, true));
    }
    else if (params.getName() == null) {
        params.setNullPrice();
        return itemRepo.findAllByPriceBetweenAndShopStatusIs(params.getPriceFloor(), params.getPriceCeil(), Shop.Status.OPEN, pageable)
                .map(entity -> ShopItemDto.fromEntity(entity, true));
    }
    else return itemRepo.findAllByNameContainingAndShopStatusIs(params.getName(), Shop.Status.OPEN, pageable)
                .map(entity -> ShopItemDto.fromEntity(entity, true));
}
```

## 상품 주문

상품 주문은 일반 사용자가 먼저 `ShopItemOrder`를 만들고, 사업자가 해당 `ShopItemOrder`의 상태를 수정하는 순서로 진행된다.
1. **일반 사용자**가 상품을 구매 요청한다.
2. **사업자 사용자**가 상품 구매를 수락, 거절한다.

또한 상황에 따라 일반 사용자가 주문을 취소할 수 있다. 이 경우, 수락, 거절되기 전에 진행해야 한다.

일반 사용자가 주문을 만들거나 취소하는 경우는 `OrderService`에,
사업자 사용자가 주문을 수락, 거절하는 경우는 `MyShopService`에 구현했다.

나의 주문을 조회, 취소하는 과정을 위해 `getOrder` 메서드를 추가하고,
`ORDERED`가 아닌 (취소, 수락, 거절)된 상태에서 변경하려고 하거나
`CANCELED`로 바꾸는 것 외에 상태로 변경하려고 하는 경우 예외를 발생시킨다.

```java
public ItemOrderDto readOne(Long orderId) {
    return ItemOrderDto.fromEntity(getOrder(orderId));
}

@Transactional
public ItemOrderDto updateState(Long orderId, ItemOrderDto dto) {
    ShopItemOrder order = getOrder(orderId);
    // 주문 상태가 아니라면 불가하다.
    if (!order.getStatus().equals(ShopItemOrder.Status.ORDERED))
        throw new ResponseStatusException(HttpStatus.FORBIDDEN);
    // 취소 상태 외로는 변경이 불가하다.
    if (!dto.getStatus().equals(ShopItemOrder.Status.CANCELED))
        throw new ResponseStatusException(HttpStatus.FORBIDDEN);
    order.setStatus(dto.getStatus());
    return ItemOrderDto.fromEntity(orderRepo.save(order));
}

private ShopItemOrder getOrder(Long orderId) {
    ShopItemOrder order = orderRepo.findById(orderId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    UserEntity user = authFacade.extractUser();
    // 내가 만든 주문이 아니라면 조회 불가하다.
    if (!order.getOrderUser().getId().equals(user.getId()))
        throw new ResponseStatusException(HttpStatus.FORBIDDEN);
    return order;
}
```

사업자 사용자 입장에서는 앞서 만든 주문 조회 외에도, 단독 주문 조회, 주문의 상태 변경을 위한 메서드를 만들었다.
또한 이들은 해당 주문의 상품이, 자신의 쇼핑몰의 것인지를 확인해야 함으로 별도의 `getOrderCheckOwner` 메서드도 만들었다.

```java
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
```

또한 `decreaseStock` 메서드를 이용해 `stock`이 0 이상으로 유지되도록 하며,
0보다 떨어질 경우 예외를 발생시킨다.

```java
// ShopItem
public void decreaseStock(Integer count) {
    if (this.stock < count) throw new ResponseStatusException(HttpStatus.CONFLICT);
    this.stock -= count;
}
```
