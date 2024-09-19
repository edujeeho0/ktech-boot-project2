package com.example.market.auth.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Arrays;
import java.util.Collection;

@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MarketUserDetails implements UserDetails {
    private Long id;
    private String username;
    private String password;
    private Integer age;
    private String email;
    private String phone;
    private String profileImg;
    private String rolesRaw;
    @Getter
    private UserEntity entity;

    public static MarketUserDetails fromEntity(UserEntity entity) {
        return MarketUserDetails.builder()
                .entity(entity)
                .build();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Arrays.stream(entity.getRoles().split(","))
                .map(role -> (GrantedAuthority) () -> role)
                .toList();
    }

    @Override
    public String getPassword() {
        return this.entity.getPassword();
    }

    @Override
    public String getUsername() {
        return this.entity.getUsername();
    }

    public Long getId() {
        return this.entity.getId();
    }

    public Integer getAge() {
        return this.entity.getAge();
    }

    public String getEmail() {
        return this.entity.getEmail();
    }

    public String getPhone() {
        return this.entity.getPhone();
    }

    public String getProfileImg() {
        return this.entity.getProfileImg();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
