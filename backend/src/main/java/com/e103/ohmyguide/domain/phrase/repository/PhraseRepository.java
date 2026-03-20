package com.e103.ohmyguide.domain.phrase.repository;

import com.e103.ohmyguide.domain.phrase.entity.Phrase;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PhraseRepository extends JpaRepository<Phrase, Long> {
}
