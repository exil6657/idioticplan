package com.zenith.client.flipping.profit;

import com.zenith.client.flipping.FlipType;

/**
 * Historical record of a completed flip. Persisted to {@code config/history.json}
 * via ConfigManager so profit stats survive restarts.
 */
public final class FlipRecord {
    public long timestampMs;
    public String itemId;
    public String itemName;
    public FlipType type;
    public long buyPrice;
    public long sellPrice;
    public long profit;
    public long heldMs;
    public boolean success;
    public String note;

    public FlipRecord() {}

    public FlipRecord(String itemId, String itemName, FlipType type, long buy, long sell,
                      long profit, long heldMs, boolean success, String note) {
        this.timestampMs = System.currentTimeMillis();
        this.itemId = itemId;
        this.itemName = itemName;
        this.type = type;
        this.buyPrice = buy;
        this.sellPrice = sell;
        this.profit = profit;
        this.heldMs = heldMs;
        this.success = success;
        this.note = note;
    }
}
