package com.e103.ohmyguide.domain.attraction.repository;

import com.e103.ohmyguide.domain.attraction.entity.Attraction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AttractionRepository extends JpaRepository<Attraction, Long> {
}
