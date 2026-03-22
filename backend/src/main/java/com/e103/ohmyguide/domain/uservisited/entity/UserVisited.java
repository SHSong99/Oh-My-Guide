package com.e103.ohmyguide.domain.uservisited.entity;

import com.e103.ohmyguide.domain.attraction.entity.Attraction;
import com.e103.ohmyguide.domain.user.entity.User;
import com.e103.ohmyguide.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(
        name = "user_visited",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "attr_id"})
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserVisited extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_visited_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attr_id", nullable = false)
    private Attraction attraction;

    @Builder
    private UserVisited(User user, Attraction attraction) {
        this.user = user;
        this.attraction = attraction;
    }
}
