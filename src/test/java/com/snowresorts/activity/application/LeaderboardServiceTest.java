package com.snowresorts.activity.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.snowresorts.activity.domain.model.LeaderboardEntry;
import com.snowresorts.activity.domain.port.Runs;
import com.snowresorts.activity.domain.port.UserAccess;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("LeaderboardService")
class LeaderboardServiceTest {

    private static final UUID SELF = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID FRIEND = UUID.fromString("22222222-2222-2222-2222-222222222222");

    @Mock private Runs runs;
    @Mock private UserAccess userAccess;

    private LeaderboardService service;

    @BeforeEach
    void setUp() {
        service = new LeaderboardService(runs, userAccess);
    }

    @Test
    void friendsLeaderboard_resolvesFriendsServerSide() {
        when(userAccess.listAcceptedFriendIds(SELF)).thenReturn(List.of(FRIEND));
        when(runs.leaderboard(any(), any(), any(), any())).thenReturn(List.of(
                new LeaderboardEntry(SELF, 40.0, 1000.0, 2, 200.0, 25.0, 600),
                new LeaderboardEntry(FRIEND, 50.0, 800.0, 1, 150.0, 30.0, 400)));

        List<LeaderboardEntry> ranking = service.friendsLeaderboard(
                SELF, LeaderboardPeriod.WEEK, null, ZoneOffset.UTC, Instant.parse("2026-01-15T12:00:00Z"));

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Set<UUID>> participants = ArgumentCaptor.forClass(Set.class);
        verify(runs).leaderboard(participants.capture(), any(), any(), eq(null));
        assertThat(participants.getValue()).containsExactlyInAnyOrder(SELF, FRIEND);
        assertThat(ranking).extracting(LeaderboardEntry::userId).containsExactly(FRIEND, SELF);
    }
}
