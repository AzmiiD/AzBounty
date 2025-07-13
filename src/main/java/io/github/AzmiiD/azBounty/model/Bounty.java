package io.github.AzmiiD.azBounty.model;

import java.util.UUID;

public class Bounty {

    private final UUID creatorUniqueId;
    private final String creatorName;
    private final UUID targetUniqueId;
    private final String targetName;
    private final double reward;
    private final long creationTimestamp;
    private final long expirationTimestamp;

    public Bounty(UUID creatorUniqueId, String creatorName, UUID targetUniqueId, String targetName, double reward, long creationTimestamp, long expirationTimestamp) {
        this.creatorUniqueId = creatorUniqueId;
        this.creatorName = creatorName;
        this.targetUniqueId = targetUniqueId;
        this.targetName = targetName;
        this.reward = reward;
        this.creationTimestamp = creationTimestamp;
        this.expirationTimestamp = expirationTimestamp;
    }

    public UUID getCreatorUniqueId() {
        return creatorUniqueId;
    }

    public String getCreatorName() {
        return creatorName;
    }

    public UUID getTargetUniqueId() {
        return targetUniqueId;
    }

    public String getTargetName() {
        return targetName;
    }

    public double getReward() {
        return reward;
    }

    public long getCreationTimestamp() {
        return creationTimestamp;
    }

    public long getExpirationTimestamp() {
        return expirationTimestamp;
    }

    public boolean isExpired() {
        if (expirationTimestamp == 0) {
            return false; // 0 means it never expires
        }
        return System.currentTimeMillis() > expirationTimestamp;
    }
}