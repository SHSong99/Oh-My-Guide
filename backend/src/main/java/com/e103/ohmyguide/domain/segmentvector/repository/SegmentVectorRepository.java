package com.e103.ohmyguide.domain.segmentvector.repository;

import com.e103.ohmyguide.domain.segmentvector.entity.SegmentVector;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SegmentVectorRepository extends JpaRepository<SegmentVector, Long> {

    Optional<SegmentVector> findBySegmentTypeAndSegmentKey(String segmentType, String segmentKey);

    List<SegmentVector> findBySegmentType(String segmentType);
}
