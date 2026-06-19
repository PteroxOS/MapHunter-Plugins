package dev.pterox.maphunter.notification;

import dev.pterox.maphunter.MapHunter;
import dev.pterox.maphunter.util.MessageUtil;
import org.bukkit.Bukkit;

import java.util.HashMap;
import java.util.Map;

public class NotificationManager {

    private final MapHunter plugin;

    public NotificationManager(MapHunter plugin) {
        this.plugin = plugin;
    }

    public void broadcastLeaderDeath(String clanName, String playerName) {
        String template = plugin.getMessageConfig().getConfig().getString("notification.leader-death", "&c☠ Ketua clan &l{clan} &r&c({player}) telah terbunuh!");
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("clan", clanName);
        placeholders.put("player", playerName);
        
        String message = MessageUtil.format(template, placeholders);
        Bukkit.broadcastMessage(message);
    }

    public void broadcastEventStart() {
        String template = plugin.getMessageConfig().getConfig().getString("notification.event-start", "&a&l[MapHunter] &r&aEvent telah dimulai!");
        Bukkit.broadcastMessage(MessageUtil.format(template, null));
        
        String titleTemplate = plugin.getMessageConfig().getConfig().getString("notification.event-start-title", "&a&lMAP HUNTER");
        String subTitleTemplate = plugin.getMessageConfig().getConfig().getString("notification.event-start-subtitle", "&eEvent telah dimulai!");
        
        String title = MessageUtil.format(titleTemplate, null);
        String subtitle = MessageUtil.format(subTitleTemplate, null);
        
        for (org.bukkit.entity.Player player : Bukkit.getOnlinePlayers()) {
            player.sendTitle(title, subtitle, 10, 70, 20);
            try {
                // In some versions it's MOB_APPEARANCE, in others ELDER_GUARDIAN
                org.bukkit.Particle particle = null;
                try {
                    particle = org.bukkit.Particle.valueOf("MOB_APPEARANCE");
                } catch (IllegalArgumentException e) {
                    try {
                        particle = org.bukkit.Particle.valueOf("ELDER_GUARDIAN");
                    } catch (IllegalArgumentException ignored) {
                    }
                }
                
                if (particle != null) {
                    player.spawnParticle(particle, player.getLocation(), 1);
                }
            } catch (Exception ignored) {
                // Ignore if particle is not supported on this version
            }
        }
    }

    public void broadcastEventStop() {
        String template = plugin.getMessageConfig().getConfig().getString("notification.event-stop", "&c&l[MapHunter] &r&cEvent telah berakhir!");
        Bukkit.broadcastMessage(MessageUtil.format(template, null));
    }
}
