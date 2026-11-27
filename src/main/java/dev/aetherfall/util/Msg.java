package dev.aetherfall.util;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

/**
 * Small helpers for colored chat messages. Legacy color codes are used on purpose:
 * they render identically on Java and Bedrock (Geyser) clients.
 */
public final class Msg {
    private Msg() {
    }

    public static String c(String text) {
        if (text == null) {
            return "";
        }
        return ChatColor.translateAlternateColorCodes('&', text);
    }

    public static void send(CommandSender to, String text) {
        to.sendMessage(c(text));
    }

    public static String bar() {
        return c("&8&m----------------------------------------");
    }
}
