package com.example.market.auth;

import com.example.market.auth.entity.MarketUserDetails;
import com.example.market.auth.entity.UserEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class AuthenticationFacade {
    public Authentication getAuth() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    public UserEntity extractUser() {
        MarketUserDetails userDetails
                = (MarketUserDetails) getAuth().getPrincipal();
        return userDetails.getEntity();
    }
}
