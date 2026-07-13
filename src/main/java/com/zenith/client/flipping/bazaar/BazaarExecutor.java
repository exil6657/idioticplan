package com.zenith.client.flipping.bazaar;

import com.zenith.client.ZenithClient;
import com.zenith.client.core.chat.ZenithChat;
import com.zenith.client.core.interaction.GUIParser;
import com.zenith.client.core.interaction.GUIState;
import com.zenith.client.core.interaction.SignInputHandler;
import com.zenith.client.core.interaction.skyblock.BazaarGUI;
import com.zenith.client.core.interaction.skyblock.BazaarNavigator;
import com.zenith.client.core.protection.BitsSpendBlocker;
import com.zenith.client.failsafe.FailsafeManager;
import com.zenith.client.flipping.FlipType;
import com.zenith.client.flipping.order.Order;
import com.zenith.client.flipping.order.OrderManager;
import com.zenith.client.flipping.order.OrderState;
import net.minecraft.client.Minecraft;

/**
 * Drives a Bazaar buy/sell order through the Bazaar GUI. State machine:
 * <pre>
 * IDLE → OPEN_BZ → WAIT_CATALOG → FIND_PRODUCT → WAIT_PRODUCT
 *   → CLICK_BUY_INSTANTLY (or CREATE_BUY_ORDER) → WAIT_QUANTITY_SIGN
 *   → SET_QUANTITY → WAIT_PRICE_SIGN → SET_PRICE (only for orders)
 *   → WAIT_CONFIRM → CLICK_CONFIRM → WAIT_FILLED → IDLE
 * </pre>
 *
 * <p>For BAZAAR_SPREAD flips we buy instantly and immediately offer a sell
 * order 0.1% under market, OR sell instantly if we're holding the item.</p>
 */
public final class BazaarExecutor {

    private static final BazaarExecutor INSTANCE = new BazaarExecutor();
    public static BazaarExecutor getInstance() { return INSTANCE; }

    private final BazaarGUI bz = new BazaarGUI();
    private Order current = null;
    private long stateEnteredMs;
    private String state = "IDLE";
    private String prevState = "";
    private boolean selling = false;
    private int retries = 0;
    private String pendingText = null;

    private BazaarExecutor() {}

    public boolean busy() { return current != null; }
    public String state() { return state; }

    public void instantBuy(Order o) {
        if (busy()) { ZenithClient.LOGGER.warn("[BZ] busy, ignoring buy for {}", o.itemId()); return; }
        if (FailsafeManager.getInstance().areMacrosPaused()) return;
        this.current = o;
        this.selling = false;
        transition("OPEN_BZ");
        BazaarNavigator.getInstance().openBazaar();
        retries = 0;
    }

    public void instantSell(Order o) {
        if (busy()) { ZenithClient.LOGGER.warn("[BZ] busy, ignoring sell for {}", o.itemId()); return; }
        if (FailsafeManager.getInstance().areMacrosPaused()) return;
        this.current = o;
        this.selling = true;
        transition("OPEN_BZ_SELL");
        BazaarNavigator.getInstance().openBazaar();
        retries = 0;
    }

    public void tick() {
        if (current == null) { state = "IDLE"; return; }
        GUIState s;
        try { s = GUIParser.getInstance().read(); } catch (Throwable t) { s = null; }
        if (s == null && !state.startsWith("OPEN_")) {
            if (retries++ > 3) fail("gui closed");
            return;
        }
        if (s != null) bz.update(s);
        long now = System.currentTimeMillis();

        switch (state) {
            case "OPEN_BZ", "OPEN_BZ_SELL" -> {
                if (BazaarNavigator.getInstance().isInBazaar() && s != null
                        && bz.detectPage(s) == BazaarGUI.Page.CATALOG) {
                    transition(selling ? "FIND_PRODUCT_SELL" : "FIND_PRODUCT");
                } else if (now - stateEnteredMs > 6000) {
                    if (retries++ > 2) fail("timeout opening bazaar");
                    else BazaarNavigator.getInstance().openBazaar();
                }
            }
            case "FIND_PRODUCT", "FIND_PRODUCT_SELL" -> {
                if (s == null) break;
                int slot = bz.findProduct(s, current.itemId(), current.candidate.itemName);
                if (slot >= 0) {
                    bz.clickProduct(s, slot);
                    transition(selling ? "WAIT_PRODUCT_SELL" : "WAIT_PRODUCT");
                } else if (now - stateEnteredMs > 4000) {
                    fail("product not found: " + current.itemId());
                }
            }
            case "WAIT_PRODUCT" -> {
                if (s == null) break;
                if (bz.detectPage(s) == BazaarGUI.Page.PRODUCT) {
                    int bi = bz.findBuyInstantly(s);
                    if (bi >= 0 && !BitsSpendBlocker.isBlocked()) {
                        bz.clickBuyInstantly(s);
                        transition("WAIT_QUANTITY");
                    } else if (now - stateEnteredMs > 3000) fail("buy-instantly button missing");
                } else if (now - stateEnteredMs > 4000) fail("product page didn't open");
            }
            case "WAIT_PRODUCT_SELL" -> {
                if (s == null) break;
                if (bz.detectPage(s) == BazaarGUI.Page.PRODUCT) {
                    int si = bz.findSellInstantly(s);
                    if (si >= 0) {
                        bz.clickSellInstantly(s);
                        transition("WAIT_QUANTITY_SELL");
                    } else if (now - stateEnteredMs > 3000) fail("sell-instantly button missing");
                } else if (now - stateEnteredMs > 4000) fail("product page didn't open");
            }
            case "WAIT_QUANTITY", "WAIT_QUANTITY_SELL" -> {
                if (SignInputHandler.getInstance().isInSignScreen()) {
                    // Buy/sell count from the candidate for instant flips.
                    pendingText = Integer.toString(Math.max(1, current.candidate.count));
                    SignInputHandler.getInstance().requestType(pendingText);
                    // Instant buy/sell goes straight to confirmation after quantity.
                    transition(selling ? "WAIT_CONFIRM_SELL" : "WAIT_CONFIRM");
                } else if (now - stateEnteredMs > 4000) {
                    transition(selling ? "WAIT_CONFIRM_SELL" : "WAIT_CONFIRM");
                }
            }
            case "WAIT_CONFIRM", "WAIT_CONFIRM_SELL" -> {
                if (s == null) break;
                BazaarGUI.Page want = selling ? BazaarGUI.Page.CONFIRM_SELL : BazaarGUI.Page.CONFIRM_BUY;
                if (bz.detectPage(s) == want) {
                    int c = bz.findConfirmButton(s);
                    if (c >= 0) {
                        bz.clickConfirm(s);
                        transition("WAIT_DONE");
                    } else if (now - stateEnteredMs > 3000) fail("confirm button missing");
                } else if (now - stateEnteredMs > 3000) {
                    // Maybe it auto-confirmed; check for catalog return.
                    transition("WAIT_DONE");
                }
            }
            case "WAIT_DONE" -> {
                if (s == null) break;
                BazaarGUI.Page p = bz.detectPage(s);
                boolean backToProduct = p == BazaarGUI.Page.PRODUCT || p == BazaarGUI.Page.CATALOG
                        || p == BazaarGUI.Page.CATEGORY;
                if (backToProduct && now - stateEnteredMs > 1200) {
                    if (selling) {
                        long sellPrice = current.listPrice > 0 ? current.listPrice : current.candidate.sellPrice;
                        OrderManager.getInstance().markCompleted(current, sellPrice);
                        ZenithChat.getInstance().success("Bazaar sell complete: {} @ {} coins.",
                                current.candidate.itemName, sellPrice);
                    } else {
                        OrderManager.getInstance().markBought(current);
                        ZenithChat.getInstance().success("Bazaar buy complete: {} @ {} coins. Now listing sell offer.",
                                current.candidate.itemName, current.buyPrice);
                    }
                    current = null; state = "IDLE"; pendingText = null; selling = false;
                } else if (now - stateEnteredMs > 8000) {
                    fail("bazaar order didn't complete");
                }
            }
        }
    }

    private void transition(String to) {
        prevState = state;
        state = to;
        stateEnteredMs = System.currentTimeMillis();
        ZenithClient.LOGGER.debug("[BZ] → {}", to);
    }

    private void fail(String reason) {
        ZenithClient.LOGGER.warn("[BZ] order {} failed: {}", current != null ? current.itemId() : "?", reason);
        if (current != null) OrderManager.getInstance().fail(current, reason);
        current = null;
        pendingText = null;
        selling = false;
        state = "IDLE";
    }
}
