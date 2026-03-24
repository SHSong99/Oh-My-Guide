package com.e103.ohmyguide.domain.userphrase.repository;

import com.e103.ohmyguide.domain.phrase.entity.Phrase;
import com.e103.ohmyguide.domain.user.entity.User;
import com.e103.ohmyguide.domain.userphrase.entity.UserPhrase;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserPhraseRepository extends JpaRepository<UserPhrase, Long> {

    boolean existsByUserAndPhrase(User user, Phrase phrase);

    void deleteByUserAndPhrase(User user, Phrase phrase);
}
