package io.github.AzmiiD.azBounty.model;

import java.util.UUID;

public class CompletedBounty {

    private final String creatorName;
    private final String targetName;
    private final String claimerName;
    private final double reward;
    private final long completionTimestamp;

    public CompletedBounty(String creatorName, String targetName, String claimerName, double reward, long completionTimestamp) {
        this.creatorName = creatorName;
        this.targetName = targetName;
        this.claimerName = claimerName;
        this.reward = reward;
        this.completionTimestamp = completionTimestamp;
    }

    public String getCreatorName() {
        return creatorName;
    }

    public String getTargetName() {
        return targetName;
    }

    public String getClaimerName() {
        return claimerName;
    }

    public double getReward() {
        return reward;
    }

    public long getCompletionTimestamp() {
        return completionTimestamp;
    }
}