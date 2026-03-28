package com.e103.ohmyguide.domain.guide.dto;

import com.e103.ohmyguide.domain.attraction.entity.Attraction;
import com.e103.ohmyguide.domain.user.entity.User;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class UserGoLogMessage {

    @NotNull
    private Long userId;

    @NotBlank
    private String nationality;

    @Positive
    private int age;

    @NotBlank
    private String gender;

    @NotBlank
    private String travelPurpose;

    @NotBlank
    private String lifestyle;

    @NotBlank
    private String action;

    @NotNull
    private Long placeId;

    private BigDecimal currentLat;

    private BigDecimal currentLng;

    private BigDecimal reachLat;

    private BigDecimal reachLng;

    private Integer star;

    @NotBlank
    private String timestamp;

    @Builder
    private UserGoLogMessage(Long userId, String nationality, int age, String gender,
                             String travelPurpose, String lifestyle, String action,
                             Long placeId, BigDecimal currentLat, BigDecimal currentLng,
                             BigDecimal reachLat, BigDecimal reachLng, Integer star,
                             String timestamp) {
        this.userId = userId;
        this.nationality = nationality;
        this.age = age;
        this.gender = gender;
        this.travelPurpose = travelPurpose;
        this.lifestyle = lifestyle;
        this.action = action;
        this.placeId = placeId;
        this.currentLat = currentLat;
        this.currentLng = currentLng;
        this.reachLat = reachLat;
        this.reachLng = reachLng;
        this.star = star;
        this.timestamp = timestamp;
    }

    public static UserGoLogMessage toMessage(User user, Long placeId,
                                             BigDecimal currentLat, BigDecimal currentLng,
                                             BigDecimal reachLat, BigDecimal reachLng) {
        return UserGoLogMessage.builder()
                .userId(user.getId())
                .nationality(user.getNationality())
                .age(user.getAge() != null ? user.getAge() : 0)
                .gender(user.getGender() != null ? user.getGender() : "unknown")
                .travelPurpose(user.getTravelPurpose() != null ? user.getTravelPurpose() : "general")
                .lifestyle(user.getLifestyle() != null ? user.getLifestyle() : "standard")
                .action("GO")
                .placeId(placeId)
                .currentLat(currentLat)
                .currentLng(currentLng)
                .reachLat(reachLat)
                .reachLng(reachLng)
                .timestamp(LocalDateTime.now().toString())
                .build();
    }

    public static UserGoLogMessage toStarMessage(User user, Long placeId, int star) {
        String action = switch (star) {
            case 3 -> "1";
            case 4 -> "2";
            case 5 -> "3";
            default -> "0";
        };
        return UserGoLogMessage.builder()
                .userId(user.getId())
                .nationality(user.getNationality())
                .age(user.getAge() != null ? user.getAge() : 0)
                .gender(user.getGender() != null ? user.getGender() : "unknown")
                .travelPurpose(user.getTravelPurpose() != null ? user.getTravelPurpose() : "general")
                .lifestyle(user.getLifestyle() != null ? user.getLifestyle() : "standard")
                .action(action)
                .placeId(placeId)
                .star(star)
                .timestamp(LocalDateTime.now().toString())
                .build();
    }

    public static UserGoLogMessage toViewMessage(User user, Long placeId) {
        return UserGoLogMessage.builder()
                .userId(user.getId())
                .nationality(user.getNationality())
                .age(user.getAge() != null ? user.getAge() : 0)
                .gender(user.getGender() != null ? user.getGender() : "unknown")
                .travelPurpose(user.getTravelPurpose() != null ? user.getTravelPurpose() : "general")
                .lifestyle(user.getLifestyle() != null ? user.getLifestyle() : "standard")
                .action("VIEW")
                .placeId(placeId)
                .timestamp(LocalDateTime.now().toString())
                .build();
    }
}