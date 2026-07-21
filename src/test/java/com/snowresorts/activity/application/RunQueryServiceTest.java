package com.snowresorts.activity.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.snowresorts.activity.domain.model.Run;
import com.snowresorts.activity.domain.model.RunMetrics;
import com.snowresorts.activity.domain.model.RunStatus;
import com.snowresorts.activity.domain.port.GpsPoints;
import com.snowresorts.activity.domain.port.RunMetricsStore;
import com.snowresorts.activity.domain.port.Runs;
import com.snowresorts.activity.domain.port.UserAccess;
import com.snowresorts.security.error.ResourceNotFoundException;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("RunQueryService access control")
class RunQueryServiceTest {

    private static final UUID OWNER = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID VIEWER = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final UUID RUN_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");

    @Mock private Runs runs;
    @Mock private RunMetricsStore metricsStore;
    @Mock private GpsPoints gpsPoints;
    @Mock private UserAccess userAccess;

    private RunQueryService service;

    @BeforeEach
    void setUp() {
        service = new RunQueryService(runs, metricsStore, gpsPoints, userAccess);
    }

    @Test
    void detail_allowsOwner() {
        Run run = completedRun(OWNER);
        when(runs.findById(RUN_ID)).thenReturn(Optional.of(run));
        when(metricsStore.findByRunId(RUN_ID)).thenReturn(Optional.of(RunMetrics.zero()));

        assertThat(service.detail(RUN_ID, OWNER).run()).isEqualTo(run);
        verifyNoInteractions(userAccess);
    }

    @Test
    void detail_allowsFriendWithShareStats() {
        Run run = completedRun(OWNER);
        when(runs.findById(RUN_ID)).thenReturn(Optional.of(run));
        when(userAccess.canViewStats(VIEWER, OWNER)).thenReturn(true);
        when(metricsStore.findByRunId(RUN_ID)).thenReturn(Optional.of(RunMetrics.zero()));

        assertThat(service.detail(RUN_ID, VIEWER).run()).isEqualTo(run);
    }

    @Test
    void detail_hidesRunWhenNotFriend() {
        Run run = completedRun(OWNER);
        when(runs.findById(RUN_ID)).thenReturn(Optional.of(run));
        when(userAccess.canViewStats(VIEWER, OWNER)).thenReturn(false);

        assertThatThrownBy(() -> service.detail(RUN_ID, VIEWER))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void history_returns404WhenShareDenied() {
        when(userAccess.canViewStats(VIEWER, OWNER)).thenReturn(false);

        assertThatThrownBy(() -> service.history(OWNER, VIEWER, null, null, 0, 20))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    private static Run completedRun(UUID userId) {
        Instant now = Instant.parse("2026-01-01T12:00:00Z");
        return new Run(RUN_ID, userId, null, null, now, now, RunStatus.COMPLETED);
    }
}

