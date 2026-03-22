package com.e103.ohmyguide.domain.uservisited.repository;

import com.e103.ohmyguide.domain.uservisited.entity.UserVisited;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserVisitedRepository extends JpaRepository<UserVisited, Long> {
}
