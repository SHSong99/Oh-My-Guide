package com.e103.ohmyguide.domain.user.repository;

import com.e103.ohmyguide.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}
