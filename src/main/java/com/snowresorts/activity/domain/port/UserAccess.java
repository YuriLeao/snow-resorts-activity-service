package com.snowresorts.activity.domain.port;

import java.util.List;
import java.util.UUID;

/** Outbound port for friendship / shareStats checks against user-service. */
public interface UserAccess {

    /** @return true when {@code viewerId} may see {@code ownerId}'s run stats and GPS. */
    boolean canViewStats(UUID viewerId, UUID ownerId);

    /** Accepted friend ids for the given user (never trusts the mobile client). */
    List<UUID> listAcceptedFriendIds(UUID userId);
}
