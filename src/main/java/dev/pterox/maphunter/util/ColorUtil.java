package dev.pterox.maphunter.util;

import org.bukkit.ChatColor;
import org.bukkit.map.MapPalette;

public class ColorUtil {

    /**
     * Converts a string color name to the corresponding MapPalette byte.
     * Default to WHITE if not recognized.
     */
    @SuppressWarnings("deprecation")
    public static byte getMapColor(String colorName) {
        if (colorName == null) return MapPalette.WHITE;
        switch (colorName.toUpperCase()) {
            case "RED":
                return MapPalette.RED;
            case "BLUE":
                return MapPalette.BLUE;
            case "GREEN":
                return MapPalette.DARK_GREEN;
            case "YELLOW":
                return MapPalette.matchColor(255, 255, 0);
            case "PURPLE":
                // No direct PURPLE in MapPalette constants, fallback to color indexes or approximate
                // For simplicity we use MapPalette.matchColor if needed, but constants are safer.
                // MapPalette has some basic colors.
                // Or just use basic colors. We'll approximate.
                return MapPalette.matchColor(128, 0, 128);
            case "ORANGE":
                return MapPalette.matchColor(255, 165, 0);
            case "AQUA":
                return MapPalette.matchColor(0, 255, 255);
            case "WHITE":
            default:
                return MapPalette.WHITE;
        }
    }

    /**
     * Converts a string color name to the corresponding ChatColor.
     */
    public static ChatColor getChatColor(String colorName) {
        if (colorName == null) return ChatColor.WHITE;
        switch (colorName.toUpperCase()) {
            case "RED":
                return ChatColor.RED;
            case "BLUE":
                return ChatColor.BLUE;
            case "GREEN":
                return ChatColor.GREEN;
            case "YELLOW":
                return ChatColor.YELLOW;
            case "PURPLE":
                return ChatColor.DARK_PURPLE;
            case "ORANGE":
                return ChatColor.GOLD;
            case "AQUA":
                return ChatColor.AQUA;
            case "WHITE":
            default:
                return ChatColor.WHITE;
        }
    }
}
