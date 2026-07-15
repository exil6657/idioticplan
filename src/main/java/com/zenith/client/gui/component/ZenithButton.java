package com.zenith.client.gui.component;
/**
 * Alias for {@link Button} — kept so older references compile. The real button
 * implementation lives in {@code Button.java}; this class just inherits all its
 * behaviour so future code can use either name.
 */
public class ZenithButton extends Button {
    public ZenithButton(String label) { super(label); }
    public ZenithButton(String label, java.util.function.Consumer<Button> onClick) { super(label, onClick); }
}
