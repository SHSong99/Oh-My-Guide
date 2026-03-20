package com.e103.ohmyguide.domain.contenttype.repository;

import com.e103.ohmyguide.domain.contenttype.entity.ContentType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContentTypeRepository extends JpaRepository<ContentType, Long> {
}
