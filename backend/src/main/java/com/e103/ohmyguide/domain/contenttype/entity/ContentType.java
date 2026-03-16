package com.e103.ohmyguide.domain.contenttype.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "contenttypes")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ContentType {

    @Id
    @Column(name = "content_type_id")
    private Integer contentTypeId;

    @Column(name = "content_type_name", length = 45)
    private String contentTypeName;

    @Builder
    private ContentType(Integer contentTypeId, String contentTypeName) {
        this.contentTypeId = contentTypeId;
        this.contentTypeName = contentTypeName;
    }
}
