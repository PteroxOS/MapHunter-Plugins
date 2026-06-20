package dev.pterox.maphunter.listeners;

import dev.pterox.maphunter.leader.LeaderData;
import dev.pterox.maphunter.leader.LeaderManager;
import dev.pterox.maphunter.notification.NotificationManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

public class PlayerDeathListener implements Listener {

    private final dev.pterox.maphunter.MapHunter plugin;
    private final LeaderManager leaderManager;
    private final NotificationManager notificationManager;

    public PlayerDeathListener(dev.pterox.maphunter.MapHunter plugin, LeaderManager leaderManager, NotificationManager notificationManager) {
        this.plugin = plugin;
        this.leaderManager = leaderManager;
        this.notificationManager = notificationManager;
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        if (leaderManager.isLeader(player)) {
            LeaderData data = leaderManager.getLeaderData(player);
            if (data != null) {
                notificationManager.broadcastLeaderDeath(data.getClanName(), player.getName());
                plugin.getMapManager().removeHunterMap(player);
                leaderManager.removeLeader(player);
                player.sendMessage(dev.pterox.maphunter.util.MessageUtil.color("&cKamu telah mati dan status leader kamu telah dicabut."));
                
                // Play Thunder Sound
                for (Player online : org.bukkit.Bukkit.getOnlinePlayers()) {
                    online.playSound(online.getLocation(), org.bukkit.Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 1.0f, 1.0f);
                }

                // Kill Rewards
                if (plugin.getConfig().getBoolean("features.kill-rewards.enabled", true)) {
                    Player killer = player.getKiller();
                    if (killer != null && leaderManager.isLeader(killer)) {
                        if (plugin.getConfig().getBoolean("features.kill-rewards.heal-full", true)) {
                            killer.setHealth(killer.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH).getValue());
                            killer.sendMessage(dev.pterox.maphunter.util.MessageUtil.color("&aDarahmu terisi penuh karena membunuh leader musuh!"));
                        }
                        for (String effectStr : plugin.getConfig().getStringList("features.kill-rewards.effects")) {
                            try {
                                String[] split = effectStr.split(":");
                                org.bukkit.potion.PotionEffectType type = org.bukkit.potion.PotionEffectType.getByName(split[0].toUpperCase());
                                int level = Integer.parseInt(split[1]) - 1; // 0-indexed
                                int duration = Integer.parseInt(split[2]) * 20; // in ticks
                                if (type != null) {
                                    killer.addPotionEffect(new org.bukkit.potion.PotionEffect(type, duration, level));
                                }
                            } catch (Exception e) {
                                plugin.getLogger().warning("Invalid kill reward effect: " + effectStr);
                            }
                        }
                    }
                }

                // Check Auto Win
                if (plugin.getEventManager().isEventActive()) {
                    if (leaderManager.getAllLeaders().size() == 1) {
                        LeaderData winnerData = leaderManager.getAllLeaders().iterator().next();
                        Player winner = org.bukkit.Bukkit.getPlayer(winnerData.getUuid());
                        if (winner != null && winner.isOnline()) {
                            plugin.getEventManager().triggerAutoWin(winner, winnerData.getClanName());
                        } else {
                            plugin.getEventManager().stopEvent();
                        }
                    } else if (leaderManager.getAllLeaders().isEmpty()) {
                        plugin.getEventManager().stopEvent();
                    }
                }
            }
        }
    }
}
