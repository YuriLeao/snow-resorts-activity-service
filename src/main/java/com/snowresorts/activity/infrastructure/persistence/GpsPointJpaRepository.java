package com.snowresorts.activity.infrastructure.persistence;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GpsPointJpaRepository extends JpaRepository<GpsPointEntity, UUID> {

    List<GpsPointEntity> findByRunIdOrderByRecordedAtAsc(UUID runId);
}
