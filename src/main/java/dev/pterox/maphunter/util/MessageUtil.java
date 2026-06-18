package dev.pterox.maphunter.util;

import org.bukkit.ChatColor;

import java.util.Map;

public class MessageUtil {

    public static String color(String text) {
        if (text == null) return "";
        return ChatColor.translateAlternateColorCodes('&', text);
    }

    public static String format(String template, Map<String, String> placeholders) {
        if (template == null) return "";
        String result = template;
        if (placeholders != null) {
            for (Map.Entry<String, String> entry : placeholders.entrySet()) {
                result = result.replace("{" + entry.getKey() + "}", entry.getValue());
            }
        }
        return color(result);
    }

    public static void sendConfigMessage(org.bukkit.command.CommandSender sender, String path, String def, dev.pterox.maphunter.MapHunter plugin) {
        String prefix = plugin.getMessageConfig().getConfig().getString("messages.prefix", "&8[&bMapHunter&8] &r");
        String message = plugin.getMessageConfig().getConfig().getString(path, def);
        sender.sendMessage(color(prefix + message));
    }
}
