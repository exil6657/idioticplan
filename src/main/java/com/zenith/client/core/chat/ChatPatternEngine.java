package com.zenith.client.core.chat;

import com.zenith.client.ZenithClient;
import com.zenith.client.core.event.ZenithEventBus;
import com.zenith.client.core.event.annotation.SubscribeEvent;
import com.zenith.client.core.event.events.*;
import com.zenith.client.core.player.SkillXPTracker;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

/**
 * Matches incoming chat lines against a list of regex patterns and fires the
 * corresponding events. All 35 Phase-2 domain events that are chat-triggered are
 * wired here.
 */
public final class ChatPatternEngine {

    private static final ChatPatternEngine INSTANCE = new ChatPatternEngine();
    public static ChatPatternEngine getInstance() { return INSTANCE; }

    private final List<ChatPattern> patterns = new ArrayList<>();
    private long coins;
    private boolean registered;

    public void init() {
        if (registered) return;
        ZenithEventBus.getInstance().register(this);
        registered = true;
        registerPatterns();
        ZenithClient.LOGGER.info("[ChatPatternEngine] {} patterns registered", patterns.size());
    }

    @SubscribeEvent
    public void onChat(ChatReceivedEvent e) {
        String msg = e.getMessage();
        if (msg == null) return;
        for (ChatPattern p : patterns) {
            Matcher m = p.regex.matcher(msg);
            if (m.matches()) {
                String[] groups = new String[m.groupCount()];
                for (int i = 0; i < m.groupCount(); i++) groups[i] = m.group(i+1);
                try { p.handler.handle(groups, msg); }
                catch (Throwable t) { ZenithClient.LOGGER.error("[ChatPatternEngine] handler failed for {}", p.regex, t); }
            }
        }
    }

    private void registerPatterns() {
        // ---- Connection/world ----
        pat("^\\s*You are in (limbo|Limbo)\\b.*", (g,r) -> post(new LimboDetectedEvent("limbo_chat")));
        pat("^\\s*You were kicked|^\\s*You are banned|^\\s*Banned.*:(.*)", (g,r) -> post(new BanDetectedEvent(g.length>0?g[0]:"ban",-1,false)));
        pat("^\\s*Muted for (\\d+)(m|h|s)", (g,r) -> post(new BanDetectedEvent("muted", Long.parseLong(g[0]), true)));
        pat("^\\s*You joined (.*)!", (g,r) -> post(new WorldChangeEvent(g[0])));

        // ---- Coop / chat / parties ----
        pat("^\\s*(?:\\[[A-Z+]+\\] )?([A-Za-z0-9_]+)[:➤](.*)", (g,r) -> {
            String sender = g[0];
            if (sender != null && g[1] != null && g[1].toLowerCase().contains("zenith")) {
                post(new CoopMessageEvent(sender, g[1]));
            }
        });

        // ---- Inventory / menu ----
        // Open/close is detected by GUIParser, not by chat.

        // ---- Items / drops ----
        // Rare drop format: "§r§6§lRARE DROP! §r§b[item]§r§e (xN)" or "EXCEPTIONAL DROP"
        pat("^.*(RARE DROP|CRAZY RARE|INSANE|PREMIUM|LEGENDARY|MYTHIC)\\s+DROP!.*\\[([^]]+)\\].*",
            (g,r) -> post(new RareDropEvent("rare", g[1], 0L)));
        pat("^.*You found a ([^!]+)!.*", (g,r) -> post(new ItemCollectedEvent(sanitise(g[0]),1,true)));

        // ---- Purse / coins ----
        pat("^.*([+-][\\d,]+) coins.*", (g,r) -> {
            long delta = parseLongSafe(g[0]);
            long newCoins = coins + delta;
            post(new PurseChangeEvent(coins, newCoins));
            coins = newCoins;
            if (delta > 0) post(new ProfitMilestoneEvent(0, 0)); // hook into StatsManager later
        });
        // ---- Sell ----
        pat("^.*You sold .* for ([\\d,]+) coins.*", (g,r) -> post(new SellCompleteEvent("sell", 1, parseLongSafe(g[0]))));

        // ---- Skills ----
        pat("^.*([A-Za-z ]+) leveled up to level (\\d+)!.*", (g,r) -> {
            String skill = sanitise(g[0]);
            int lvl = Integer.parseInt(g[1].replaceAll(",","").trim());
            SkillXPTracker.getInstance().setLevel(skill, lvl);
            post(new SkillLevelUpEvent(skill, lvl));
        });

        // ---- Garden ----
        pat("^.*Garden Level Up!.*Level (\\d+).*", (g,r) -> post(new GardenLevelUpEvent(Integer.parseInt(g[0]))));
        pat("^.*([A-Za-z ]+) Milestone (\\d+).*", (g,r) -> post(new CropMilestoneEvent(sanitise(g[0]), Integer.parseInt(g[1]))));
        // ---- Jacob contest ----
        pat("^.*Jacob's Contest is starting! Crop: ([A-Za-z ]+).*", (g,r) -> post(new JacobContestStartEvent(sanitise(g[0]), 1200_000)));
        // ---- Pests ----
        pat("^.*A (.*) Pest.*", (g,r) -> post(new PestSpawnEvent(sanitise(g[0]))));
        // ---- Visitors ----
        pat("^.*Visitor: ([A-Za-z ]+) is here.*", (g,r) -> post(new VisitorArrivedEvent(sanitise(g[0]), "")));

        // ---- Community upgrade ----
        pat("^.*Community Upgrade ([A-Za-z ]+) is ready.*", (g,r) -> post(new CommunityUpgradeReadyEvent(sanitise(g[0]))));
        // ---- Museum ----
        pat("^.*Museum Milestone: ([^!]+)!", (g,r) -> post(new MuseumMilestoneEvent(sanitise(g[0]))));
        // ---- Mayor ----
        pat("^.*Mayor (.*) is now in office.*", (g,r) -> post(new MayorChangeEvent(sanitise(g[0]), "", true)));

        // ---- Failsafe / player nearby ----
        pat("^.*([A-Za-z0-9_]+) has come close to you.*", (g,r) -> post(new PlayerNearbyEvent(sanitise(g[0]), 8)));

        // ---- Macro / error / stuck / break ----
        pat("^.*Macro started: ([^.]+)", (g,r) -> post(new MacroStartEvent(sanitise(g[0]))));
        pat("^.*Macro stopped: ([^.]+)", (g,r) -> post(new MacroStopEvent(sanitise(g[0]), "chat")));
        pat("^.*Break started.*", (g,r) -> post(new BreakStartEvent(60_000)));
        pat("^.*Break ended.*", (g,r) -> post(new BreakEndEvent(0)));

        // ---- Failsafe ----
        // FailsafeTriggerEvent is fired directly by failsafes, not by chat patterns.

        // ---- Flip ----
        pat("^.*Flip completed: ([^ ]+) for ([\\d,]+) coins profit.*",
                (g,r) -> post(new FlipCompleteEvent(sanitise(g[0]), parseLongSafe(g[1]), 0)));
        pat("^.*BIG FLIP: ([^ ]+) \\+([\\d,]+) coins.*",
                (g,r) -> post(new BigFlipCompleteEvent(sanitise(g[0]), parseLongSafe(g[1]))));

        // ---- Disconnect/reconnect ----
        pat("^.*Disconnected.*", (g,r) -> post(new DisconnectEvent("disconnect")));
        pat("^.*Reconnected to.*", (g,r) -> post(new ReconnectEvent("", 0)));

        // ---- PIN ----
        pat("^.*PIN (correct|incorrect).*",
                (g,r) -> post(new PINAttemptEvent("correct".equalsIgnoreCase(g[0]))));
    }

    private void pat(String regex, ChatPattern.ChatHandler handler) {
        patterns.add(new ChatPattern(regex, handler));
    }
    private static String sanitise(String s) { return s == null ? "" : s.replaceAll("§.", "").trim(); }
    private static long parseLongSafe(String s) {
        try { return Long.parseLong(s.replaceAll("[^0-9-]", "")); }
        catch (Exception e) { return 0; }
    }
    private static void post(Object e) { ZenithEventBus.getInstance().post(e); }

    public long getPurse() { return coins; }
    public void setPurse(long c) { this.coins = c; }
}
