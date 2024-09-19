# 관리자 기능

관리자는 사용자의 권한을 조정하거나, 특정 정보의 공개 비공개를 전환하기 위해 확인하는 절차에
개입하는 기능을 가지고 있다. 이는 일반적인 CRUD에서 크게 벗어나지 않지만,
Spring Security를 이용해 접근 가능한 사용자를 조정함으로서 달성한다.

```java
@Service
@RequiredArgsConstructor
public class AdminService {
    private final UserRepo userRepo;
    private final UserUpgradeRepo userUpgradeRepo;
    private final ShopRepo shopRepo;
    private final ShopOpenReqRepo openRepo;

    // 사용자 권한 업그레이드 요청 읽기
    public Page<UserUpgradeDto> listRequests(Pageable pageable) {
        return userUpgradeRepo.findAll(pageable)
                .map(UserUpgradeDto::fromEntity);
    }

    // 사용자 권한 업그레이드 수락
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

    // 사용자 권한 업그레이드 거절
    @Transactional
    public UserUpgradeDto disapproveUpgrade(Long id) {
        UserUpgrade upgrade = userUpgradeRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        upgrade.setApproved(false);
        return UserUpgradeDto.fromEntity(upgrade);
    }

    // 상점 개설 요청 읽기
    public Page<ShopDto> readOpenRequests(Pageable pageable) {
        return shopRepo.findOpenRequested(pageable)
                .map(ShopDto::fromEntity);
    }

    // 상점 폐쇄 요청 읽기
    public Page<ShopDto> readCloseRequests(Pageable pageable) {
        return shopRepo.findCloseRequested(Shop.Status.CLOSED, pageable)
                .map(shop -> ShopDto.fromEntity(shop, true));
    }

    // 상점 개설 허가
    public void approveOpen(Long shopId) {
        ShopOpenRequest openRequest = openRepo.findTopByShopIdAndIsApprovedIsNullOrderByCreatedAtDesc(shopId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST));
        openRequest.setIsApproved(true);
        openRequest.getShop().setStatus(Shop.Status.OPEN);
        openRepo.save(openRequest);
    }

    // 상점 개설 거절
    public void disapproveOpen(Long shopId, OpenRequestDto dto) {
        ShopOpenRequest openRequest = openRepo.findTopByShopIdAndIsApprovedIsNullOrderByCreatedAtDesc(shopId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST));
        openRequest.setIsApproved(false);
        openRequest.setReason(dto.getReason());
        openRequest.getShop().setStatus(Shop.Status.REJECTED);
        openRepo.save(openRequest);
    }

    // 상점 폐쇄 허가
    public void approveClose(Long shopId) {
        Shop shop = shopRepo.findById(shopId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (shop.getCloseReason() == null || shop.getStatus() != Shop.Status.OPEN)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);

        shop.setStatus(Shop.Status.CLOSED);
        shopRepo.save(shop);
    }
}

```