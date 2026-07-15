package com.zenith.client.core.chat;

/**
 * Chat prefix tokens that colour-code the "Zenith >>" tag.
 *
 * <p>Uses § formatting codes. These are applied <em>only</em> to client-side
 * messages — we never inject custom strings into server-bound packets
 * (master rule §4: server invisibility).</p>
 */
public enum ChatPrefix {

    DEFAULT("§bZenith §7»§r "),
    WARN("§eZenith §7»§r "),
    ERROR("§cZenith §7»§r "),
    DEBUG("§8[Zenith Debug]§r "),
    SUCCESS("§aZenith §7»§r ");

    private final String literal;

    ChatPrefix(String literal) {
        this.literal = literal;
    }

    public String getLiteral() {
        return literal;
    }
}
