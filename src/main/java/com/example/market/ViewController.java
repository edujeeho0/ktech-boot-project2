package com.example.market;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("views")
public class ViewController {
    @GetMapping
    public String index() {
        return "index";
    }

    @GetMapping("login")
    public String login() {
        return "login";
    }

    @GetMapping("signup")
    public String signUp() {
        return "signup";
    }

    @GetMapping("user/update")
    public String userUpdate() {
        return "user/update";
    }

    @GetMapping("user/upgrade")
    public String userUpgrade() {
        return "user/upgrade";
    }

    @GetMapping("admin/upgrade-requests")
    public String adminCheckRequests() {
        return "admin/upgrade-requests";
    }

    @GetMapping("admin/shop-open-requests")
    public String adminShopOpenRequests() {
        return "admin/shop-open-requests";
    }

    @GetMapping("admin/shop-close-requests")
    public String adminShopCloseRequests() {
        return "admin/shop-close-requests";
    }

    @GetMapping("shops")
    public String shops() {
        return "shops/index";
    }

    @GetMapping("search/shops")
    public String shopsSearch() {
        return "search/shops";
    }

    @GetMapping("search/items")
    public String shopsItemSearch() {
        return "search/items";
    }

    @GetMapping("shops/{shopId}")
    public String shopIndex() {
        return "shops/items/index";
    }

    @GetMapping("shops/{shopId}/items/{itemId}")
    public String shopItem() {
        return "shops/items/item";
    }

    @GetMapping("shops/my-shop")
    public String myShop() {
        return "shops/my-shop/index";
    }

    @GetMapping("shops/my-shop/items")
    public String myShopItems() {
        return "shops/my-shop/items/index";
    }

    @GetMapping("shops/my-shop/items/{itemId}")
    public String myShopItem() {
        return "shops/my-shop/items/update";
    }

    @GetMapping("shops/my-shop/orders")
    public String myShopOrders() {
        return "shops/my-shop/orders";
    }

    @GetMapping("orders")
    public String myOrders() {
        return "orders";
    }
}
