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
        String template = plugin.getMessageConfig().getConfig().getString("notification.leader-actionbar", "&bKamu adalah leader team {clan}");
        
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (leaderManager.isLeader(player)) {
                LeaderData data = leaderManager.getLeaderData(player);
                if (data != null) {
                    Map<String, String> placeholders = new HashMap<>();
                    placeholders.put("clan", data.getClanName());
                    
                    String message = MessageUtil.format(template, placeholders);
                    
                    try {
                        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(message));
                    } catch (Exception e) {
                        try {
                            player.sendActionBar(message);
                        } catch (Exception ignored) {
                            // Fallback if neither is supported
                        }
                    }
                }
            }
        }
    }
}
