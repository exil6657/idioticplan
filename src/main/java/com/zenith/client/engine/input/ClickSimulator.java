package com.zenith.client.engine.input;

/**
 * High-level click controller: schedules left/right clicks at humanised
 * intervals using the {@code clicking} sub-package (ClickHumanizer, InterClickDelay,
 * HoldDurationVariation, etc.).
 */
public final class ClickSimulator {

    private final MouseSimulator mouse;
    private boolean leftHeld, rightHeld;

    public ClickSimulator(MouseSimulator mouse) { this.mouse = mouse; }

    public void pressLeft()   { leftHeld = true; }
    public void releaseLeft() { leftHeld = false; }
    public void pressRight()  { rightHeld = true; }
    public void releaseRight(){ rightHeld = false; }

    public void clickLeft()  { mouse.leftClick(1); }
    public void clickRight() { mouse.rightClick(1); }

    public boolean leftHeld()  { return leftHeld; }
    public boolean rightHeld() { return rightHeld; }
}
