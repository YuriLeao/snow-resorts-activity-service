package com.snowresorts.activity.infrastructure.web;

import com.snowresorts.activity.application.LeaderboardPeriod;
import com.snowresorts.activity.application.LeaderboardService;
import com.snowresorts.activity.domain.model.LeaderboardEntry;
import com.snowresorts.security.SecurityUtils;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/snow-resort-service/v1/leaderboard")
public class LeaderboardController {

    private final LeaderboardService leaderboardService;

    public LeaderboardController(LeaderboardService leaderboardService) {
        this.leaderboardService = leaderboardService;
    }

    /**
     * Ranks the caller plus the supplied friend ids for the period. {@code friendIds} comes from the
     * client (sourced from user-service) for the MVP; {@code period} is
     * {@code today|week|season|year|all}.
     * Optional {@code resortId} limits the ranking to descents at that resort.
     * {@code timeZone} is the caller's IANA zone (from the phone) so today/year match their calendar.
     */
    @GetMapping("/friends")
    public List<LeaderboardEntryResponse> friends(
            @RequestParam(required = false) String period,
            @RequestParam(required = false) List<UUID> friendIds,
            @RequestParam(required = false) UUID resortId,
            @RequestParam(required = false) String timeZone) {
        UUID userId = SecurityUtils.requireCurrentUserId();
        List<LeaderboardEntry> ranking = leaderboardService.friendsLeaderboard(
                userId,
                friendIds,
                LeaderboardPeriod.fromParam(period),
                resortId,
                LeaderboardPeriod.resolveZone(timeZone),
                Instant.now());

        List<LeaderboardEntryResponse> response = new java.util.ArrayList<>(ranking.size());
        for (int i = 0; i < ranking.size(); i++) {
            response.add(LeaderboardEntryResponse.of(i + 1, ranking.get(i)));
        }
        return response;
    }
}
