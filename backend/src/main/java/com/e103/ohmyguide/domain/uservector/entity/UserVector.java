package com.e103.ohmyguide.domain.uservector.entity;

import com.e103.ohmyguide.domain.user.entity.User;
import com.e103.ohmyguide.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "user_vectors")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserVector extends BaseEntity {

    @Id
    @Column(name = "user_id")
    private Long userId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "preference_vector", nullable = false, columnDefinition = "json")
    private String preferenceVector;

    @Builder
    private UserVector(User user, String preferenceVector) {
        this.user = user;
        this.preferenceVector = preferenceVector;
    }

    public void updatePreferenceVector(String preferenceVector) {
        this.preferenceVector = preferenceVector;
    }
}
