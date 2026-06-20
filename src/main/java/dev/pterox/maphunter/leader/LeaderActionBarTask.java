package dev.pterox.maphunter.leader;

import dev.pterox.maphunter.MapHunter;
import dev.pterox.maphunter.util.MessageUtil;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;

public class LeaderActionBarTask extends BukkitRunnable {

    private final MapHunter plugin;
    private final LeaderManager leaderManager;

    public LeaderActionBarTask(MapHunter plugin, LeaderManager leaderManager) {
        this.plugin = plugin;
        this.leaderManager = leaderManager;
    }

    @Override
    public void run() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (leaderManager.isLeader(player)) {
                LeaderData data = leaderManager.getLeaderData(player);
                if (data != null) {
                    String colorCode = getColorCode(data.getClanColor());
                    
                    // Kita override config agar langsung pakai warna clan & punya progress bar text
                    String message = colorCode + "Kamu adalah leader team " + data.getClanName() + " &8[" + colorCode + "■■■■■■■■■■&8]";
                    message = MessageUtil.color(message);
                    
                    try {
                        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(message));
                    } catch (Exception e) {
                        try {
                            player.sendActionBar(message);
                        } catch (Exception ignored) {
                        }
                    }
                }
            }
        }
    }

    private String getColorCode(String colorName) {
        if (colorName == null) return "§f";
        switch (colorName.toUpperCase()) {
            case "RED": return "§c";
            case "BLUE": return "§9";
            case "GREEN": return "§a";
            case "AQUA": return "§b";
            case "YELLOW": return "§e";
            case "PURPLE": return "§5";
            case "ORANGE": return "§6";
            case "PINK": return "§d";
            default: return "§f";
        }
    }
}
