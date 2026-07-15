package com.zenith.client.engine.input;

/**
 * Simulates key presses via Minecraft's keybinding system.
 *
 * <p>Phase 2/3 API surface: concrete wiring to {@code KeyBinding.setPressed}
 * happens in Phase 4 when MixinKeyboardHandler is filled in. This class
 * accumulates desired key states per tick so the input engine doesn't call
 * Minecraft directly.</p>
 */
public final class KeySimulator {

    private boolean forward, back, left, right, jump, sneak, sprint, use, attack;

    public void setForward(boolean v)  { forward = v; }
    public void setBack(boolean v)     { back = v; }
    public void setLeft(boolean v)     { left = v; }
    public void setRight(boolean v)    { right = v; }
    public void setJump(boolean v)     { jump = v; }
    public void setSneak(boolean v)    { sneak = v; }
    public void setSprint(boolean v)   { sprint = v; }
    public void setUse(boolean v)      { use = v; }
    public void setAttack(boolean v)   { attack = v; }

    public void halt() { forward=back=left=right=jump=sneak=sprint=use=attack=false; }

    public boolean forward() { return forward; }
    public boolean back()    { return back; }
    public boolean left()    { return left; }
    public boolean right()   { return right; }
    public boolean jump()    { return jump; }
    public boolean sneak()   { return sneak; }
    public boolean sprint()  { return sprint; }
    public boolean use()     { return use; }
    public boolean attack()  { return attack; }

    /** Called once per tick by the input engine to push state to MC. Phase 4 wires KeyBinding. */
    public void flush() {
        // Phase 4: set KeyBinding pressed state on Options keys.
    }
}
