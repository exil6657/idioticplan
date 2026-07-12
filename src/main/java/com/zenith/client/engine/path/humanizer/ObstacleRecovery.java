package com.zenith.client.engine.path.humanizer;

/**
 * When the player bumps something (velocity drops, not moving), performs a
 * small recovery routine: back off slightly, strafe, jump once, re-path.
 * This mimics a real player reacting to a block in their face rather than
 * continuing to hold W into a wall.
 */
public final class ObstacleRecovery {

    public enum RecoveryStage { NONE, BACKOFF, STRAFE, JUMP, REPATH }

    private RecoveryStage stage = RecoveryStage.NONE;
    private long stageEndAt;

    public void trigger(long nowMs) {
        if (stage != RecoveryStage.NONE) return;
        stage = RecoveryStage.BACKOFF;
        stageEndAt = nowMs + 180;
    }

    public RecoveryStage tick(long nowMs) {
        if (stage == RecoveryStage.NONE) return stage;
        if (nowMs >= stageEndAt) {
            stage = switch (stage) {
                case BACKOFF -> { stageEndAt = nowMs + 120; yield RecoveryStage.STRAFE; }
                case STRAFE  -> { stageEndAt = nowMs + 250; yield RecoveryStage.JUMP; }
                case JUMP    -> { stageEndAt = nowMs + 100; yield RecoveryStage.REPATH; }
                case REPATH, NONE -> RecoveryStage.NONE;
            };
        }
        return stage;
    }

    public void clear() { stage = RecoveryStage.NONE; }
    public boolean active() { return stage != RecoveryStage.NONE; }
}
