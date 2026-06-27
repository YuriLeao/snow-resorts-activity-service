package com.snowresorts.activity.infrastructure.web;

import com.snowresorts.activity.application.RunQueryService;
import com.snowresorts.activity.application.RunQueryService.RunHistoryPage;
import com.snowresorts.activity.application.RunTrackingService;
import com.snowresorts.activity.application.RunTrackingService.AppendResult;
import com.snowresorts.activity.domain.model.Run;
import com.snowresorts.activity.domain.model.RunMetrics;
import com.snowresorts.activity.domain.model.TrackPoint;
import com.snowresorts.security.SecurityUtils;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/snow-resort-service/v1/runs")
public class RunController {

    private final RunTrackingService trackingService;
    private final RunQueryService queryService;
    private final IdempotencyStore idempotencyStore;

    public RunController(RunTrackingService trackingService, RunQueryService queryService,
                        IdempotencyStore idempotencyStore) {
        this.trackingService = trackingService;
        this.queryService = queryService;
        this.idempotencyStore = idempotencyStore;
    }

    @PostMapping("/start")
    @ResponseStatus(HttpStatus.CREATED)
    public RunResponse start(@Valid @RequestBody StartRunRequest request) {
        UUID userId = SecurityUtils.requireCurrentUserId();
        Run run = trackingService.start(userId, request.resortId(), request.trailId(), request.startedAt());
        return RunResponse.from(run);
    }

    @PatchMapping("/{id}/points")
    public BatchPointsResponse appendPoints(
            @PathVariable UUID id,
            @Valid @RequestBody BatchPointsRequest request,
            @RequestHeader(name = "Idempotency-Key", required = false) String idempotencyKey) {
        UUID userId = SecurityUtils.requireCurrentUserId();

        if (idempotencyKey != null && !idempotencyKey.isBlank() && !idempotencyStore.register(idempotencyKey)) {
            return new BatchPointsResponse(0, 0, true);
        }

        List<TrackPoint> points = request.points().stream().map(TrackPointDto::toDomain).toList();
        AppendResult result = trackingService.appendPoints(id, userId, points);
        return new BatchPointsResponse(result.accepted(), result.rejected(), false);
    }

    @PostMapping("/{id}/finish")
    public RunSummaryResponse finish(
            @PathVariable UUID id,
            @RequestBody(required = false) FinishRunRequest request) {
        UUID userId = SecurityUtils.requireCurrentUserId();
        RunMetrics metrics = trackingService.finish(id, userId,
                request != null ? request.endedAt() : null);
        Run run = queryService.detail(id, userId).run();
        return RunSummaryResponse.of(run, metrics);
    }

    @GetMapping
    public List<RunSummaryResponse> history(
            @RequestParam(required = false) LocalDate date,
            @RequestParam(required = false) UUID resortId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        UUID userId = SecurityUtils.requireCurrentUserId();
        RunHistoryPage result = queryService.history(userId, date, resortId, page, size);
        return result.content().stream().map(RunSummaryResponse::from).toList();
    }

    @GetMapping("/{id}")
    public RunDetailResponse detail(@PathVariable UUID id) {
        UUID userId = SecurityUtils.requireCurrentUserId();
        return RunDetailResponse.from(queryService.detail(id, userId));
    }

    @GetMapping("/{id}/replay")
    public ReplayResponse replay(@PathVariable UUID id) {
        UUID userId = SecurityUtils.requireCurrentUserId();
        return ReplayResponse.of(id, queryService.replay(id, userId));
    }
}
