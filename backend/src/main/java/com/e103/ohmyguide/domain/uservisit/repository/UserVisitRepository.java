package com.e103.ohmyguide.domain.uservisit.repository;

import com.e103.ohmyguide.domain.uservisit.entity.UserVisit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface UserVisitRepository extends JpaRepository<UserVisit, Long> {

    @Query("SELECT uv.attrId FROM UserVisit uv WHERE uv.userId = :userId")
    List<Long> findAttrIdsByUserId(Long userId);

    boolean existsByUserIdAndAttrId(Long userId, Long attrId);
}
