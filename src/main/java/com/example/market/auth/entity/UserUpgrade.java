package com.example.market.auth.entity;

import com.example.market.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Builder
@Entity
@Table(name = "role_upgrade_req")
@NoArgsConstructor
@AllArgsConstructor
public class UserUpgrade extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    private UserEntity target;

    @Setter
    private Boolean approved;
}
