package dev.pterox.maphunter.listeners;

import dev.pterox.maphunter.leader.LeaderData;
import dev.pterox.maphunter.leader.LeaderManager;
import dev.pterox.maphunter.notification.NotificationManager;
import dev.pterox.maphunter.util.ItemUtil;
import dev.pterox.maphunter.util.LogUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;

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
        
        boolean isMainLeader = leaderManager.isLeader(player);
        boolean isBackupLeader = hasBackupMap(player) || plugin.getMapManager().hasBackupMapTracked(player.getUniqueId());
        boolean hasAnyMap = isMainLeader || isBackupLeader;
        
        if (!hasAnyMap) return;
        
        LeaderData data = leaderManager.getLeaderData(player);
        String clanName = data != null ? data.getClanName() : "Unknown";
        String playerName = player.getName();
        Player killer = player.getKiller();
        String killerName = killer != null ? killer.getName() : null;
        
        plugin.getLogger().info("[MapHunter] Leader/backup mati: " + playerName + " (clan: " + clanName + ")");
        LogUtil.logLeaderDeath(playerName, clanName, killerName);
        
        notificationManager.broadcastLeaderDeath(clanName, playerName, killerName);
        plugin.getMapManager().removeHunterMap(player);
        plugin.getMapManager().removeBackupMap(player);
        leaderManager.removeLeader(player);
        
        player.sendMessage(dev.pterox.maphunter.util.MessageUtil.color(""));
        player.sendMessage(dev.pterox.maphunter.util.MessageUtil.color("&c&m                              "));
        player.sendMessage(dev.pterox.maphunter.util.MessageUtil.color("&8[&b&lMapHunter&8] &r&c&l☠ KAMU TELAH MATI"));
        player.sendMessage(dev.pterox.maphunter.util.MessageUtil.color("&8[&b&lMapHunter&8] &r&fStatus leader kamu telah dicabut."));
        player.sendMessage(dev.pterox.maphunter.util.MessageUtil.color("&c&m                              "));
        player.sendMessage(dev.pterox.maphunter.util.MessageUtil.color(""));

        if (plugin.getConfig().getBoolean("features.kill-rewards.enabled", true)) {
            if (killer != null && (leaderManager.isLeader(killer) || hasBackupMap(killer))) {
                if (plugin.getConfig().getBoolean("features.kill-rewards.heal-full", true)) {
                    killer.setHealth(killer.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH).getValue());
                    killer.sendMessage(dev.pterox.maphunter.util.MessageUtil.color(""));
                    killer.sendMessage(dev.pterox.maphunter.util.MessageUtil.color("&a&m                              "));
                    killer.sendMessage(dev.pterox.maphunter.util.MessageUtil.color("&8[&b&lMapHunter&8] &r&a&l⚡ KILL REWARD"));
                    killer.sendMessage(dev.pterox.maphunter.util.MessageUtil.color("&8[&b&lMapHunter&8] &r&fDarah terisi penuh!"));
                    killer.sendMessage(dev.pterox.maphunter.util.MessageUtil.color("&a&m                              "));
                    killer.sendMessage(dev.pterox.maphunter.util.MessageUtil.color(""));
                }
                for (String effectStr : plugin.getConfig().getStringList("features.kill-rewards.effects")) {
                    try {
                        String[] split = effectStr.split(":");
                        org.bukkit.potion.PotionEffectType type = org.bukkit.potion.PotionEffectType.getByName(split[0].toUpperCase());
                        int level = Integer.parseInt(split[1]) - 1;
                        int duration = Integer.parseInt(split[2]) * 20;
                        if (type != null) {
                            killer.addPotionEffect(new org.bukkit.potion.PotionEffect(type, duration, level));
                        }
                    } catch (Exception e) {
                        plugin.getLogger().warning("Invalid kill reward effect: " + effectStr);
                    }
                }
            }
        }

        if (plugin.getEventManager().isEventActive()) {
            List<LeaderData> remaining = leaderManager.getAllLeaders();
            plugin.getLogger().info("[MapHunter] Sisa leader: " + remaining.size());
            
            if (remaining.size() == 1) {
                LeaderData winnerData = remaining.iterator().next();
                Player winner = org.bukkit.Bukkit.getPlayer(winnerData.getUuid());
                plugin.getLogger().info("[MapHunter] Auto-win: " + winnerData.getClanName() + " (winner: " + (winner != null ? winner.getName() : "null") + ")");
                if (winner != null && winner.isOnline()) {
                    plugin.getEventManager().triggerAutoWin(winner, winnerData.getClanName());
                } else {
                    plugin.getEventManager().stopEvent();
                }
            } else if (remaining.isEmpty()) {
                plugin.getEventManager().stopEvent();
            }
        }
    }

    private boolean hasBackupMap(Player player) {
        for (ItemStack item : player.getInventory().getContents()) {
            if (ItemUtil.isBackupMap(item)) {
                return true;
            }
        }
        for (ItemStack item : player.getInventory().getArmorContents()) {
            if (ItemUtil.isBackupMap(item)) {
                return true;
            }
        }
        return false;
    }
}
