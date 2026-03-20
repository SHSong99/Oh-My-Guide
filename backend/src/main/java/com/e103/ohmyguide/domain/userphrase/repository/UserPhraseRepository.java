package com.e103.ohmyguide.domain.userphrase.repository;

import com.e103.ohmyguide.domain.userphrase.entity.UserPhrase;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserPhraseRepository extends JpaRepository<UserPhrase, Long> {
}
