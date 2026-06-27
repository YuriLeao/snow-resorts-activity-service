package com.snowresorts.activity.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.snowresorts.activity.domain.metrics.MetricsCalculator;
import com.snowresorts.activity.domain.model.Run;
import com.snowresorts.activity.domain.model.RunMetrics;
import com.snowresorts.activity.domain.model.RunStatus;
import com.snowresorts.activity.domain.model.TrackPoint;
import com.snowresorts.activity.domain.port.GpsPoints;
import com.snowresorts.activity.domain.port.RunEventPublisher;
import com.snowresorts.activity.domain.port.RunMetricsStore;
import com.snowresorts.activity.domain.port.Runs;
import com.snowresorts.contracts.events.RunCompletedEvent;
import com.snowresorts.security.error.ConflictException;
import com.snowresorts.security.error.ForbiddenException;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("RunTrackingService")
class RunTrackingServiceTest {

    private static final UUID USER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID OTHER_USER = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final UUID RESORT_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");
    private static final UUID RUN_ID = UUID.fromString("44444444-4444-4444-4444-444444444444");
    private static final Instant T0 = Instant.parse("2026-01-15T09:00:00Z");

    @Mock
    private Runs runs;
    @Mock
    private GpsPoints gpsPoints;
    @Mock
    private RunMetricsStore metricsStore;
    @Mock
    private RunEventPublisher eventPublisher;

    private RunTrackingService service;

    @BeforeEach
    void setUp() {
        service = new RunTrackingService(runs, gpsPoints, metricsStore, eventPublisher,
                new MetricsCalculator());
    }

    private Run activeRunOwnedBy(UUID owner) {
        return new Run(RUN_ID, owner, RESORT_ID, null, T0, null, RunStatus.ACTIVE);
    }

    @Test
    @DisplayName("start_createsRun_savesActiveRunForUser")
    void start_createsRun_savesActiveRunForUser() {
        // Arrange
        when(runs.save(any(Run.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        Run created = service.start(USER_ID, RESORT_ID, null, T0);

        // Assert
        ArgumentCaptor<Run> captor = ArgumentCaptor.forClass(Run.class);
        verify(runs).save(captor.capture());
        Run saved = captor.getValue();
        assertThat(saved.status()).isEqualTo(RunStatus.ACTIVE);
        assertThat(saved.userId()).isEqualTo(USER_ID);
        assertThat(saved.resortId()).isEqualTo(RESORT_ID);
        assertThat(saved.startedAt()).isEqualTo(T0);
        assertThat(saved.endedAt()).isNull();
        assertThat(created.status()).isEqualTo(RunStatus.ACTIVE);
    }

    @Test
    @DisplayName("finish_activeRun_computesMetricsPersistsAndPublishesEvent")
    void finish_activeRun_computesMetricsPersistsAndPublishesEvent() {
        // Arrange
        when(runs.findById(RUN_ID)).thenReturn(Optional.of(activeRunOwnedBy(USER_ID)));
        when(runs.save(any(Run.class))).thenAnswer(inv -> inv.getArgument(0));
        List<TrackPoint> points = List.of(
                new TrackPoint(T0, 45.000, 6.000, 2000.0, null),
                new TrackPoint(T0.plusSeconds(10), 45.001, 6.000, 1990.0, 40.0),
                new TrackPoint(T0.plusSeconds(20), 45.002, 6.000, 1980.0, 50.0));
        when(gpsPoints.findByRunId(RUN_ID)).thenReturn(points);

        // Act
        RunMetrics metrics = service.finish(RUN_ID, USER_ID, T0.plusSeconds(20));

        // Assert: run flipped to COMPLETED with an end timestamp
        ArgumentCaptor<Run> runCaptor = ArgumentCaptor.forClass(Run.class);
        verify(runs).save(runCaptor.capture());
        assertThat(runCaptor.getValue().status()).isEqualTo(RunStatus.COMPLETED);
        assertThat(runCaptor.getValue().endedAt()).isEqualTo(T0.plusSeconds(20));

        // metrics persisted
        verify(metricsStore).save(eq(RUN_ID), eq(metrics));

        // RunCompletedEvent published carrying the computed metrics
        ArgumentCaptor<RunCompletedEvent> eventCaptor = ArgumentCaptor.forClass(RunCompletedEvent.class);
        verify(eventPublisher).publish(eventCaptor.capture());
        RunCompletedEvent event = eventCaptor.getValue();
        assertThat(event.runId()).isEqualTo(RUN_ID);
        assertThat(event.userId()).isEqualTo(USER_ID);
        assertThat(event.resortId()).isEqualTo(RESORT_ID);
        assertThat(event.maxSpeedKmh()).isEqualTo(metrics.maxSpeedKmh());
        assertThat(event.distanceM()).isEqualTo(metrics.distanceM());
        assertThat(event.durationSec()).isEqualTo(metrics.durationSec());
    }

    @Test
    @DisplayName("finish_runOwnedByAnotherUser_throwsForbidden")
    void finish_runOwnedByAnotherUser_throwsForbidden() {
        when(runs.findById(RUN_ID)).thenReturn(Optional.of(activeRunOwnedBy(OTHER_USER)));

        assertThatThrownBy(() -> service.finish(RUN_ID, USER_ID, null))
                .isInstanceOf(ForbiddenException.class);

        verify(metricsStore, never()).save(any(), any());
        verify(eventPublisher, never()).publish(any());
    }

    @Test
    @DisplayName("finish_alreadyCompletedRun_throwsConflict")
    void finish_alreadyCompletedRun_throwsConflict() {
        Run completed = new Run(RUN_ID, USER_ID, RESORT_ID, null, T0, T0.plusSeconds(30),
                RunStatus.COMPLETED);
        when(runs.findById(RUN_ID)).thenReturn(Optional.of(completed));

        assertThatThrownBy(() -> service.finish(RUN_ID, USER_ID, null))
                .isInstanceOf(ConflictException.class);

        verify(eventPublisher, never()).publish(any());
    }

    @Test
    @DisplayName("appendPoints_activeRun_filtersSpikesBeforeStoring")
    void appendPoints_activeRun_filtersSpikesBeforeStoring() {
        when(runs.findById(RUN_ID)).thenReturn(Optional.of(activeRunOwnedBy(USER_ID)));
        List<TrackPoint> points = List.of(
                new TrackPoint(T0, 45.000, 6.000, 2000.0, 30.0),
                // 2 km jump spike one second later
                new TrackPoint(T0.plusSeconds(1), 45.020, 6.000, 1999.0, 300.0),
                new TrackPoint(T0.plusSeconds(10), 45.001, 6.000, 1990.0, 40.0));

        RunTrackingService.AppendResult result = service.appendPoints(RUN_ID, USER_ID, points);

        assertThat(result.accepted()).isEqualTo(2);
        assertThat(result.rejected()).isEqualTo(1);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<TrackPoint>> captor = ArgumentCaptor.forClass(List.class);
        verify(gpsPoints).appendAll(eq(RUN_ID), captor.capture());
        assertThat(captor.getValue()).hasSize(2);
    }
}
