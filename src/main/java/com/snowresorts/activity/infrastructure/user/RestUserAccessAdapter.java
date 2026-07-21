package com.snowresorts.activity.infrastructure.user;

import com.snowresorts.activity.application.InternalApiProperties;
import com.snowresorts.activity.domain.port.UserAccess;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class RestUserAccessAdapter implements UserAccess {

    private static final Logger log = LoggerFactory.getLogger(RestUserAccessAdapter.class);

    private final RestClient userServiceClient;
    private final InternalApiProperties internalApi;

    public RestUserAccessAdapter(@Qualifier("userServiceRestClient") RestClient userServiceClient,
                                 InternalApiProperties internalApi) {
        this.userServiceClient = userServiceClient;
        this.internalApi = internalApi;
    }

    @Override
    public boolean canViewStats(UUID viewerId, UUID ownerId) {
        if (viewerId.equals(ownerId)) {
            return true;
        }
        try {
            StatsAccessResponse response = userServiceClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/snow-resort-service/v1/users/internal/access/stats")
                            .queryParam("viewerId", viewerId)
                            .queryParam("ownerId", ownerId)
                            .build())
                    .header(internalApi.header(), internalApi.secret())
                    .retrieve()
                    .body(StatsAccessResponse.class);
            return response != null && response.allowed();
        } catch (Exception ex) {
            log.warn("Stats access check failed for viewer {} owner {}: {}",
                    viewerId, ownerId, ex.getMessage());
            return false;
        }
    }

    @Override
    public List<UUID> listAcceptedFriendIds(UUID userId) {
        try {
            FriendIdsResponse response = userServiceClient.get()
                    .uri("/snow-resort-service/v1/users/internal/friends/{userId}/accepted-ids", userId)
                    .header(internalApi.header(), internalApi.secret())
                    .retrieve()
                    .body(FriendIdsResponse.class);
            if (response == null || response.friendIds() == null) {
                return List.of();
            }
            return response.friendIds();
        } catch (Exception ex) {
            log.warn("Failed to load friend ids for {}: {}", userId, ex.getMessage());
            return List.of();
        }
    }

    private record StatsAccessResponse(boolean allowed) {
    }

    private record FriendIdsResponse(List<UUID> friendIds) {
    }
}
