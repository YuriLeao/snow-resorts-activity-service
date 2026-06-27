package com.snowresorts.activity.infrastructure.persistence;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RunTrackJpaRepository extends JpaRepository<RunTrackEntity, UUID> {
}
