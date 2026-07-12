package com.zenith.client.flipping.profit;

import com.zenith.client.ZenithClient;
import com.zenith.client.core.chat.ZenithChat;
import com.zenith.client.core.event.ZenithEventBus;
import com.zenith.client.core.event.events.BigFlipCompleteEvent;
import com.zenith.client.core.event.events.FlipCompleteEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;

/** Tracks session and lifetime profit from flips; posts FlipComplete events. */
public final class ProfitTracker {

    private static final ProfitTracker INSTANCE = new ProfitTracker();
    public static ProfitTracker getInstance() { return INSTANCE; }

    private final List<FlipRecord> records = new CopyOnWriteArrayList<>();
    private final AtomicLong sessionProfit = new AtomicLong();
    private final AtomicLong sessionFlips = new AtomicLong();
    private long bestFlip = 0L;
    private String bestItem = "";

    private ProfitTracker() {}

    public void record(FlipRecord r) {
        records.add(r);
        if (r.success) {
            sessionProfit.addAndGet(r.profit);
            sessionFlips.incrementAndGet();
            if (r.profit > bestFlip) { bestFlip = r.profit; bestItem = r.itemName; }
            ZenithEventBus.getInstance().post(new FlipCompleteEvent(r.itemName, r.count, r.profit));
            if (r.profit >= 1_000_000L) {
                ZenithEventBus.getInstance().post(new BigFlipCompleteEvent(r.itemName, r.profit));
                ZenithChat.getInstance().success("BIG FLIP: {} +{} coins", r.itemName, r.profit);
            } else {
                ZenithChat.getInstance().info("Flip: {} +{} coins", r.itemName, r.profit);
            }
        }
        ZenithClient.LOGGER.info("[Flip] {} {}: buy={} sell={} profit={} held={}ms success={}",
                r.type, r.itemId, r.buyPrice, r.sellPrice, r.profit, r.heldMs, r.success);
    }

    public long sessionProfit() { return sessionProfit.get(); }
    public long sessionFlips() { return sessionFlips.get(); }
    public long bestFlipProfit() { return bestFlip; }
    public String bestFlipItem() { return bestItem; }
    public List<FlipRecord> recent(int n) {
        int size = records.size();
        if (n >= size) return new ArrayList<>(records);
        List<FlipRecord> out = new ArrayList<>(records.subList(size - n, size));
        Collections.reverse(out);
        return out;
    }
    public void resetSession() { sessionProfit.set(0); sessionFlips.set(0); bestFlip = 0; bestItem = ""; }
}
