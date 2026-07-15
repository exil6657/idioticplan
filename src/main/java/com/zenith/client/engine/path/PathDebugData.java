package com.zenith.client.engine.path;

import java.util.List;

/** Debug snapshot for Brain View. */
public record PathDebugData(
        String state,
        String mode,
        int plannedNodes,
        int executedNodes,
        double totalCost,
        double distanceToTarget,
        double currentSpeedBps,
        boolean stuck,
        String reaction,
        long computeMs
) {}
