package com.e103.ohmyguide.domain.contenttype.repository;

import com.e103.ohmyguide.IntegrationTestSupport;
import com.e103.ohmyguide.domain.contenttype.entity.ContentType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class ContentTypeRepositoryTest extends IntegrationTestSupport {

    @Autowired
    private ContentTypeRepository contentTypeRepository;

    @DisplayName("ContentType 을 저장하고 ID 로 조회한다.")
    @Test
    void saveAndFindById() {
        // given
        ContentType contentType = ContentType.builder()
                .contentTypeId(12L)
                .contentTypeName("관광지")
                .build();

        // when
        contentTypeRepository.save(contentType);
        Optional<ContentType> found = contentTypeRepository.findById(12L);

        // then
        assertThat(found).isPresent();
        assertThat(found.get().getContentTypeName()).isEqualTo("관광지");
    }

    @DisplayName("여러 ContentType 을 저장하고 전체 조회한다.")
    @Test
    void saveAllAndFindAll() {
        // given
        contentTypeRepository.save(ContentType.builder().contentTypeId(12L).contentTypeName("관광지").build());
        contentTypeRepository.save(ContentType.builder().contentTypeId(32L).contentTypeName("숙박").build());
        contentTypeRepository.save(ContentType.builder().contentTypeId(39L).contentTypeName("음식점").build());

        // when
        List<ContentType> all = contentTypeRepository.findAll();

        // then
        assertThat(all).hasSize(3)
                .extracting(ContentType::getContentTypeName)
                .containsExactlyInAnyOrder("관광지", "숙박", "음식점");
    }

    @DisplayName("ContentType 을 삭제한다.")
    @Test
    void delete() {
        // given
        contentTypeRepository.save(ContentType.builder().contentTypeId(12L).contentTypeName("관광지").build());

        // when
        contentTypeRepository.deleteById(12L);

        // then
        assertThat(contentTypeRepository.findById(12L)).isEmpty();
    }
}
