package com.zenith.client.gui.animation;

/**
 * Critically-damped spring — used for smooth GUI open/close and panel slides.
 * Models m*a + c*v + k*(x-target) = 0.
 */
public final class SpringAnimation {
    private double value;
    private double velocity;
    private double target;
    private double stiffness = 220d;
    private double damping = 22d;   // 2*sqrt(stiffness) = critical damping
    private boolean active;

    public SpringAnimation(double initial) { this.value = this.target = initial; }

    public void setStiffness(double s) { this.stiffness = s; this.damping = 2*Math.sqrt(s); }
    public void snap(double v) { this.value = this.target = v; this.velocity = 0; active=false; }
    public void animateTo(double target) { this.target = target; this.active = true; }

    public void update(float dtSec) {
        if (!active) return;
        double displacement = value - target;
        double force = -stiffness * displacement - damping * velocity;
        velocity += force * dtSec;
        value += velocity * dtSec;
        if (Math.abs(velocity) < 0.005 && Math.abs(displacement) < 0.005) {
            value = target; velocity = 0; active = false;
        }
    }

    public double get() { return value; }
    public boolean isActive() { return active; }
}
