package com.example.market.admin;

import com.example.market.auth.dto.UserDto;
import com.example.market.admin.dto.UserUpgradeDto;
import com.example.market.shop.dto.OpenRequestDto;
import com.example.market.shop.dto.ShopDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@RestController
@RequestMapping("admin")
@RequiredArgsConstructor
public class AdminController {
    private final AdminService service;

    @GetMapping("users")
    public Page<UserDto> readAllUsers(
            Pageable pageable
    ) {
        return service.readUsersPage(pageable);
    }

    @GetMapping("upgrades")
    public Page<UserUpgradeDto> upgradeRequests(
            Pageable pageable
    ) {
        return service.listRequests(pageable);
    }

    @PutMapping("upgrades/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public UserUpgradeDto approve(
            @PathVariable("id")
            Long id
    ) {
        return service.approveUpgrade(id);
    }

    @DeleteMapping("upgrades/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public UserUpgradeDto disapprove(
            @PathVariable("id")
            Long id
    ) {
        return service.disapproveUpgrade(id);
    }

    @GetMapping("shops")
    public Page<ShopDto> statusRequests(
            @RequestParam("request")
            String request,
            Pageable pageable
    ) {
        if (request.equals("OPEN"))
            return service.readOpenRequests(pageable);
        else if (request.equals("CLOSE"))
            return service.readCloseRequests(pageable);
        else throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
    }

    @PutMapping(value = "shops/{shopId}", params = "action=approve")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void approveOpen(
            @PathVariable("shopId")
            Long shopId
    ) {
        service.approveOpen(shopId);
    }

    @PutMapping(value = "shops/{shopId}", params = "action=disapprove")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void disapproveOpen(
            @PathVariable("shopId")
            Long shopId,
            @RequestBody
            OpenRequestDto dto
    ) {
        service.disapproveOpen(shopId, dto);
    }

    @DeleteMapping("shops/{shopId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void approveClose(
            @PathVariable("shopId")
            Long shopId
    ) {
        service.approveClose(shopId);
    }
}
