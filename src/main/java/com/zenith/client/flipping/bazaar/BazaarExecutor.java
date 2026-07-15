package com.zenith.client.flipping.bazaar;

import com.zenith.client.ZenithClient;
import com.zenith.client.core.chat.ZenithChat;
import com.zenith.client.core.interaction.GUIParser;
import com.zenith.client.core.interaction.GUIState;
import com.zenith.client.core.interaction.SignInputHandler;
import com.zenith.client.core.interaction.skyblock.BazaarCategory;
import com.zenith.client.core.interaction.skyblock.BazaarGUI;
import com.zenith.client.core.interaction.skyblock.BazaarNavigator;
import com.zenith.client.core.protection.BitsSpendBlocker;
import com.zenith.client.failsafe.FailsafeManager;
import com.zenith.client.flipping.FlipType;
import com.zenith.client.flipping.order.Order;
import com.zenith.client.flipping.order.OrderManager;
import com.zenith.client.flipping.order.OrderState;

/**
 * Drives Bazaar interactions through the GUI. Four entry points:
 * <ul>
 *   <li>{@link #instantBuy(Order)} — instant buy (quantity → confirm).</li>
 *   <li>{@link #instantSell(Order)} — instant sell (quantity → confirm).</li>
 *   <li>{@link #createBuyOrder(Order, long, int)} — limit buy (qty → price → confirm).</li>
 *   <li>{@link #createSellOrder(Order, long, int)} — limit sell (qty → price → confirm).</li>
 * </ul>
 *
 * <p>Always walks Catalog → Category → Product; category resolved from
 * {@link BazaarCategory#forProduct(String)}.</p>
 */
public final class BazaarExecutor {

    private static final BazaarExecutor INSTANCE = new BazaarExecutor();
    public static BazaarExecutor getInstance() { return INSTANCE; }

    private enum Mode { IDLE, INSTANT_BUY, INSTANT_SELL, CREATE_BUY, CREATE_SELL }

    private final BazaarGUI bz = new BazaarGUI();
    private Order current;
    private long stateEnteredMs;
    private String state = "IDLE";
    private Mode mode = Mode.IDLE;
    private int retries;
    private long pendingPrice = -1L;
    private int pendingCount = 1;
    private String category;

    private BazaarExecutor() {}

    public boolean busy() { return current != null; }
    public String state() { return state; }
    public Mode mode()   { return mode; }

    public void instantBuy(Order o) {
        if (busy()) { ZenithClient.LOGGER.warn("[BZ] busy, ignoring buy for {}", o.itemId()); return; }
        if (FailsafeManager.getInstance().areMacrosPaused()) return;
        this.current = o;
        this.mode = Mode.INSTANT_BUY;
        this.pendingCount = Math.max(1, o.candidate.count);
        this.pendingPrice = -1L;
        this.category = BazaarCategory.forProduct(o.itemId());
        openBz("OPEN_BZ");
    }

    public void instantSell(Order o) {
        if (busy()) { ZenithClient.LOGGER.warn("[BZ] busy, ignoring sell for {}", o.itemId()); return; }
        if (FailsafeManager.getInstance().areMacrosPaused()) return;
        this.current = o;
        this.mode = Mode.INSTANT_SELL;
        this.pendingCount = Math.max(1, o.candidate.count);
        this.pendingPrice = -1L;
        this.category = BazaarCategory.forProduct(o.itemId());
        openBz("OPEN_BZ_SELL");
    }

    public void createBuyOrder(Order o, long pricePerUnit, int count) {
        if (busy()) { ZenithClient.LOGGER.warn("[BZ] busy, ignoring create-buy for {}", o.itemId()); return; }
        if (FailsafeManager.getInstance().areMacrosPaused()) return;
        this.current = o;
        this.mode = Mode.CREATE_BUY;
        this.pendingPrice = Math.max(1L, pricePerUnit);
        this.pendingCount = Math.max(1, count);
        this.category = BazaarCategory.forProduct(o.itemId());
        openBz("OPEN_BZ_CREATE_BUY");
    }

    public void createSellOrder(Order o, long pricePerUnit, int count) {
        if (busy()) { ZenithClient.LOGGER.warn("[BZ] busy, ignoring create-sell for {}", o.itemId()); return; }
        if (FailsafeManager.getInstance().areMacrosPaused()) return;
        this.current = o;
        this.mode = Mode.CREATE_SELL;
        this.pendingPrice = Math.max(1L, pricePerUnit);
        this.pendingCount = Math.max(1, count);
        this.category = BazaarCategory.forProduct(o.itemId());
        openBz("OPEN_BZ_CREATE_SELL");
    }

    public void tick() {
        if (current == null) { state = "IDLE"; mode = Mode.IDLE; return; }
        GUIState s;
        try { s = GUIParser.getInstance().read(); } catch (Throwable t) { s = null; }
        if (s == null && !state.startsWith("OPEN_")) {
            if (retries++ > 6) fail("gui closed");
            return;
        }
        if (s != null) bz.update(s);
        long now = System.currentTimeMillis();

        switch (state) {
            case "OPEN_BZ", "OPEN_BZ_SELL", "OPEN_BZ_CREATE_BUY", "OPEN_BZ_CREATE_SELL" -> {
                if (BazaarNavigator.getInstance().isInBazaar() && s != null
                        && bz.detectPage(s) == BazaarGUI.Page.CATALOG) {
                    transition("FIND_CATEGORY");
                } else if (now - stateEnteredMs > 7000) {
                    if (retries++ > 2) fail("timeout opening bazaar");
                    else BazaarNavigator.getInstance().openBazaar();
                }
            }
            case "FIND_CATEGORY" -> {
                if (s == null) break;
                if (bz.detectPage(s) != BazaarGUI.Page.CATALOG) { transition("FIND_PRODUCT"); break; }
                int slot = bz.findCategory(s, category);
                if (slot >= 0) { bz.clickCategory(s, category); transition("WAIT_CATEGORY"); }
                else if (now - stateEnteredMs > 3500) {
                    if (!BazaarCategory.ODDITIES.equals(category)) {
                        category = BazaarCategory.ODDITIES;
                        transition("FIND_CATEGORY");
                    } else fail("category not found: " + category);
                }
            }
            case "WAIT_CATEGORY" -> {
                if (s == null) break;
                BazaarGUI.Page p = bz.detectPage(s);
                if (p == BazaarGUI.Page.CATEGORY || p == BazaarGUI.Page.PRODUCT) transition("FIND_PRODUCT");
                else if (now - stateEnteredMs > 3000) transition("FIND_PRODUCT");
            }
            case "FIND_PRODUCT" -> {
                if (s == null) break;
                int slot = bz.findProduct(s, current.itemId(), current.candidate.itemName);
                if (slot >= 0) { bz.clickProduct(s, slot); transition("WAIT_PRODUCT"); }
                else if (now - stateEnteredMs > 4500) fail("product not found: " + current.itemId());
            }
            case "WAIT_PRODUCT" -> {
                if (s == null) break;
                if (bz.detectPage(s) == BazaarGUI.Page.PRODUCT) transition("PRODUCT_READY");
                else if (now - stateEnteredMs > 4000) fail("product page didn't open");
            }
            case "PRODUCT_READY" -> {
                if (s == null) break;
                switch (mode) {
                    case INSTANT_BUY -> {
                        int bi = bz.findBuyInstantly(s);
                        if (bi >= 0 && !BitsSpendBlocker.isBlocked()) {
                            bz.clickBuyInstantly(s); transition("WAIT_QUANTITY");
                        } else if (now - stateEnteredMs > 3000) fail("buy-instantly button missing");
                    }
                    case INSTANT_SELL -> {
                        int si = bz.findSellInstantly(s);
                        if (si >= 0) { bz.clickSellInstantly(s); transition("WAIT_QUANTITY_SELL"); }
                        else if (now - stateEnteredMs > 3000) fail("sell-instantly button missing");
                    }
                    case CREATE_BUY -> {
                        int cb = bz.findCreateBuyOrder(s);
                        if (cb >= 0 && !BitsSpendBlocker.isBlocked()) {
                            bz.clickCreateBuyOrder(s); transition("WAIT_QUANTITY_CREATE_BUY");
                        } else if (now - stateEnteredMs > 3000) fail("create-buy-order button missing");
                    }
                    case CREATE_SELL -> {
                        int cs = bz.findCreateSellOrder(s);
                        if (cs >= 0) { bz.clickCreateSellOrder(s); transition("WAIT_QUANTITY_CREATE_SELL"); }
                        else if (now - stateEnteredMs > 3000) fail("create-sell-order button missing");
                    }
                }
            }
            case "WAIT_QUANTITY", "WAIT_QUANTITY_SELL",
                 "WAIT_QUANTITY_CREATE_BUY", "WAIT_QUANTITY_CREATE_SELL" -> {
                if (SignInputHandler.getInstance().isInSignScreen()) {
                    SignInputHandler.getInstance().requestType(Integer.toString(Math.max(1, pendingCount)));
                    transition(nextAfterQuantity());
                } else if (s != null) {
                    BazaarGUI.Page p = bz.detectPage(s);
                    if (p == BazaarGUI.Page.CONFIRM_BUY || p == BazaarGUI.Page.CONFIRM_SELL) transition(nextAfterQuantity());
                } else if (now - stateEnteredMs > 5000) {
                    if (retries++ > 1) fail("quantity sign didn't open");
                    else transition("WAIT_PRODUCT");
                }
            }
            case "WAIT_PRICE_CREATE_BUY", "WAIT_PRICE_CREATE_SELL" -> {
                if (SignInputHandler.getInstance().isInSignScreen()) {
                    SignInputHandler.getInstance().requestType(formatPrice(pendingPrice));
                    transition(mode == Mode.CREATE_BUY ? "WAIT_CONFIRM_BUY_ORDER" : "WAIT_CONFIRM_SELL_ORDER");
                } else if (s != null) {
                    BazaarGUI.Page p = bz.detectPage(s);
                    if (p == BazaarGUI.Page.CONFIRM_BUY || p == BazaarGUI.Page.CONFIRM_SELL)
                        transition(mode == Mode.CREATE_BUY ? "WAIT_CONFIRM_BUY_ORDER" : "WAIT_CONFIRM_SELL_ORDER");
                } else if (now - stateEnteredMs > 5000) {
                    if (retries++ > 1) fail("price sign didn't open");
                    else transition("PRODUCT_READY");
                }
            }
            case "WAIT_CONFIRM", "WAIT_CONFIRM_SELL" -> {
                if (s == null) break;
                BazaarGUI.Page want = mode == Mode.INSTANT_SELL ? BazaarGUI.Page.CONFIRM_SELL : BazaarGUI.Page.CONFIRM_BUY;
                if (bz.detectPage(s) == want) {
                    int c = bz.findConfirmButton(s);
                    if (c >= 0) { bz.clickConfirm(s); transition("WAIT_DONE"); }
                    else if (now - stateEnteredMs > 3000) fail("confirm button missing");
                } else if (now - stateEnteredMs > 4000) transition("WAIT_DONE");
            }
            case "WAIT_CONFIRM_BUY_ORDER", "WAIT_CONFIRM_SELL_ORDER" -> {
                if (s == null) break;
                BazaarGUI.Page want = mode == Mode.CREATE_SELL ? BazaarGUI.Page.CONFIRM_SELL : BazaarGUI.Page.CONFIRM_BUY;
                if (bz.detectPage(s) == want) {
                    int c = bz.findConfirmButton(s);
                    if (c >= 0) { bz.clickConfirm(s); transition("WAIT_ORDER_PLACED"); }
                    else if (now - stateEnteredMs > 3000) fail("order confirm button missing");
                } else if (now - stateEnteredMs > 4000) transition("WAIT_ORDER_PLACED");
            }
            case "WAIT_DONE" -> {
                if (s == null) break;
                BazaarGUI.Page p = bz.detectPage(s);
                boolean back = p == BazaarGUI.Page.PRODUCT || p == BazaarGUI.Page.CATALOG
                        || p == BazaarGUI.Page.CATEGORY || p == BazaarGUI.Page.MANAGE_ORDERS;
                if (back && now - stateEnteredMs > 1200) completeInstant();
                else if (now - stateEnteredMs > 8000) fail("bazaar order didn't confirm (check chat)");
            }
            case "WAIT_ORDER_PLACED" -> {
                if (s == null) break;
                BazaarGUI.Page p = bz.detectPage(s);
                boolean back = p == BazaarGUI.Page.PRODUCT || p == BazaarGUI.Page.CATALOG
                        || p == BazaarGUI.Page.CATEGORY || p == BazaarGUI.Page.MANAGE_ORDERS;
                if (back && now - stateEnteredMs > 1400) completeOrderPlaced();
                else if (now - stateEnteredMs > 8000) fail("order placement didn't confirm");
            }
        }
    }

    private void openBz(String firstState) {
        transition(firstState);
        BazaarNavigator.getInstance().openBazaar();
        retries = 0;
    }

    private String nextAfterQuantity() {
        return switch (mode) {
            case INSTANT_BUY  -> "WAIT_CONFIRM";
            case INSTANT_SELL -> "WAIT_CONFIRM_SELL";
            case CREATE_BUY   -> "WAIT_PRICE_CREATE_BUY";
            case CREATE_SELL  -> "WAIT_PRICE_CREATE_SELL";
            default           -> "WAIT_DONE";
        };
    }

    private void completeInstant() {
        if (current == null) { reset(); return; }
        if (mode == Mode.INSTANT_SELL) {
            long sellPrice = current.listPrice > 0 ? current.listPrice : current.candidate.sellPrice;
            OrderManager.getInstance().markCompleted(current, sellPrice);
            ZenithChat.getInstance().success("Bazaar instant-sell: {} × {} @ {} coins.",
                    current.candidate.itemName, pendingCount, sellPrice);
        } else {
            current.buyPrice = pendingPrice > 0 ? pendingPrice * pendingCount : current.candidate.buyPrice;
            OrderManager.getInstance().markBoughtBazaar(current);
            ZenithChat.getInstance().success("Bazaar instant-buy: {} × {} (total {} coins). Now queuing sell order.",
                    current.candidate.itemName, pendingCount, current.buyPrice);
        }
        reset();
    }

    private void completeOrderPlaced() {
        if (current == null) { reset(); return; }
        if (mode == Mode.CREATE_BUY) {
            current.buyPrice = pendingPrice * pendingCount;
            current.listPrice = pendingPrice;
            current.transition(OrderState.NAVIGATING);
            ZenithChat.getInstance().success("Bazaar buy order placed: {} × {} @ {} coins/unit.",
                    current.candidate.itemName, pendingCount, pendingPrice);
        } else {
            current.listPrice = pendingPrice;
            current.transition(OrderState.LISTED);
            ZenithChat.getInstance().success("Bazaar sell order placed: {} × {} @ {} coins/unit.",
                    current.candidate.itemName, pendingCount, pendingPrice);
        }
        reset();
    }

    private void reset() {
        current = null;
        pendingPrice = -1L;
        pendingCount = 1;
        mode = Mode.IDLE;
        state = "IDLE";
        category = null;
        retries = 0;
    }

    private void transition(String to) {
        state = to;
        stateEnteredMs = System.currentTimeMillis();
        ZenithClient.LOGGER.debug("[BZ] → {}", to);
    }

    private void fail(String reason) {
        ZenithClient.LOGGER.warn("[BZ] order {} failed (mode={}): {}",
                current != null ? current.itemId() : "?", mode, reason);
        if (current != null) OrderManager.getInstance().fail(current, reason);
        reset();
    }

    private static String formatPrice(long price) {
        if (price >= 1_000_000_000L) return String.format("%.2fB", price / 1_000_000_000d);
        if (price >= 1_000_000L)     return String.format("%.2fM", price / 1_000_000d);
        if (price >= 1_000L)         return String.format("%.1fk", price / 1_000d);
        return Long.toString(Math.max(1L, price));
    }
}
