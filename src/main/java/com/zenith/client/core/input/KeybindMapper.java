package com.zenith.client.core.input;

/**
 * Logical keybind ids for Minecraft actions.
 *
 * <p><b>Master rule §9:</b> never assume W = forward — read everything through
 * {@link PlayerKeybindReader}. Macros reference these logical ids; the reader
 * resolves them to the user's configured GLFW key codes (which may be AZERTY,
 * arrow keys, mouse binds, etc.).</p>
 */
public enum KeybindMapper {

    FORWARD,
    BACK,
    LEFT,
    RIGHT,
    JUMP,
    SNEAK,
    SPRINT,
    ATTACK,
    USE,     // right click / place
    USE_BLOCK,
    DROP,
    INVENTORY,
    CHAT,
    COMMAND,
    SWAP_HANDS,
    HOTBAR_1,
    HOTBAR_2,
    HOTBAR_3,
    HOTBAR_4,
    HOTBAR_5,
    HOTBAR_6,
    HOTBAR_7,
    HOTBAR_8,
    HOTBAR_9,
    TOGGLE_PERSPECTIVE,
    Screenshot,
    TOGGLE_FULLSCREEN;

    public String mcName() {
        return switch (this) {
            case FORWARD     -> "key.forward";
            case BACK        -> "key.back";
            case LEFT        -> "key.left";
            case RIGHT       -> "key.right";
            case JUMP        -> "key.jump";
            case SNEAK       -> "key.sneak";
            case SPRINT      -> "key.sprint";
            case ATTACK      -> "key.attack";
            case USE, USE_BLOCK -> "key.use";
            case DROP        -> "key.drop";
            case INVENTORY   -> "key.inventory";
            case CHAT        -> "key.chat";
            case COMMAND     -> "key.command";
            case SWAP_HANDS  -> "key.swapHands";
            case HOTBAR_1    -> "key.hotbar.1";
            case HOTBAR_2    -> "key.hotbar.2";
            case HOTBAR_3    -> "key.hotbar.3";
            case HOTBAR_4    -> "key.hotbar.4";
            case HOTBAR_5    -> "key.hotbar.5";
            case HOTBAR_6    -> "key.hotbar.6";
            case HOTBAR_7    -> "key.hotbar.7";
            case HOTBAR_8    -> "key.hotbar.8";
            case HOTBAR_9    -> "key.hotbar.9";
            case TOGGLE_PERSPECTIVE -> "key.togglePerspective";
            case Screenshot  -> "key.screenshot";
            case TOGGLE_FULLSCREEN -> "key.fullscreen";
        };
    }
}
