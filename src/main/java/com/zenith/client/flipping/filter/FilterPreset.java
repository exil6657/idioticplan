package com.zenith.client.flipping.filter;

/** Saved named filter preset. */
public class FilterPreset {
    public String name = "Default";
    public ItemFilter filter = new ItemFilter();

    public FilterPreset() {}
    public FilterPreset(String name, ItemFilter filter) {
        this.name = name; this.filter = filter;
    }

    public static FilterPreset lowball() {
        ItemFilter f = new ItemFilter(); f.presetLowball();
        return new FilterPreset("lowball", f);
    }
    public static FilterPreset highVolume() {
        ItemFilter f = new ItemFilter(); f.presetHighVolume();
        return new FilterPreset("high-volume", f);
    }
    public static FilterPreset bigTicket() {
        ItemFilter f = new ItemFilter(); f.presetBigTicket();
        return new FilterPreset("big-ticket", f);
    }
}
