package com.e103.ohmyguide.domain.uservisit.entity;

import com.e103.ohmyguide.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(
        name = "user_visited_places",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "attr_id"})
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserVisit extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "attr_id", nullable = false)
    private Long attrId;

    @Builder
    private UserVisit(Long userId, Long attrId) {
        this.userId = userId;
        this.attrId = attrId;
    }
}
