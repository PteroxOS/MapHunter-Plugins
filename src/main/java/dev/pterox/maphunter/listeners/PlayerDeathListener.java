package dev.pterox.maphunter.listeners;

import dev.pterox.maphunter.MapHunter;
import dev.pterox.maphunter.integration.BetterTeamsIntegration;
import dev.pterox.maphunter.leader.LeaderData;
import dev.pterox.maphunter.leader.LeaderManager;
import dev.pterox.maphunter.notification.NotificationManager;
import dev.pterox.maphunter.util.ItemUtil;
import dev.pterox.maphunter.util.LogUtil;
import dev.pterox.maphunter.util.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.UUID;

public class PlayerDeathListener implements Listener {

    private final MapHunter plugin;
    private final LeaderManager leaderManager;
    private final NotificationManager notificationManager;

    public PlayerDeathListener(MapHunter plugin, LeaderManager leaderManager, NotificationManager notificationManager) {
        this.plugin = plugin;
        this.leaderManager = leaderManager;
        this.notificationManager = notificationManager;
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        
        if (!plugin.getEventManager().isEventActive()) return;
        
        boolean isMainLeader = leaderManager.isLeader(player);
        boolean isBackupLeader = hasBackupMap(player) || plugin.getMapManager().hasBackupMapTracked(player.getUniqueId());
        boolean hasAnyMap = isMainLeader || isBackupLeader;
        
        // === LEADER/BACKUP DEATH ===
        if (hasAnyMap) {
            LeaderData data = leaderManager.getLeaderData(player);
            String clanName = data != null ? data.getClanName() : "Unknown";
            String playerName = player.getName();
            Player killer = player.getKiller();
            String killerName = killer != null ? killer.getName() : null;
            
            LogUtil.logLeaderDeath(playerName, clanName, killerName);
            
            notificationManager.broadcastLeaderDeath(clanName, playerName, killerName);
            plugin.getMapManager().removeHunterMap(player);
            plugin.getMapManager().removeBackupMap(player);
            leaderManager.removeLeader(player);
            
            player.sendMessage(MessageUtil.color(""));
            player.sendMessage(MessageUtil.color("&c&m                              "));
            player.sendMessage(MessageUtil.color("&8[&b&lMapHunter&8] &r&c&l☠ KAMU TELAH MATI"));
            player.sendMessage(MessageUtil.color("&8[&b&lMapHunter&8] &r&fStatus leader kamu telah dicabut."));
            player.sendMessage(MessageUtil.color("&c&m                              "));
            player.sendMessage(MessageUtil.color(""));

            // Kill Rewards
            if (plugin.getConfig().getBoolean("features.kill-rewards.enabled", true)) {
                if (killer != null && (leaderManager.isLeader(killer) || hasBackupMap(killer))) {
                    if (plugin.getConfig().getBoolean("features.kill-rewards.heal-full", true)) {
                        killer.setHealth(killer.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH).getValue());
                        killer.sendMessage(MessageUtil.color(""));
                        killer.sendMessage(MessageUtil.color("&a&m                              "));
                        killer.sendMessage(MessageUtil.color("&8[&b&lMapHunter&8] &r&a&l⚡ KILL REWARD"));
                        killer.sendMessage(MessageUtil.color("&8[&b&lMapHunter&8] &r&fDarah terisi penuh!"));
                        killer.sendMessage(MessageUtil.color("&a&m                              "));
                        killer.sendMessage(MessageUtil.color(""));
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

            // Auto Win
            List<LeaderData> remaining = leaderManager.getAllLeaders();
            if (remaining.size() == 1) {
                LeaderData winnerData = remaining.iterator().next();
                Player winner = Bukkit.getPlayer(winnerData.getUuid());
                if (winner != null && winner.isOnline()) {
                    plugin.getEventManager().triggerAutoWin(winner, winnerData.getClanName());
                } else {
                    plugin.getEventManager().stopEvent();
                }
            } else if (remaining.isEmpty()) {
                plugin.getEventManager().stopEvent();
            }
            return;
        }

        // === MEMBER DEATH (BetterTeams) ===
        if (plugin.getConfig().getBoolean("features.member-notification.enabled", true)) {
            handleMemberDeath(player);
        }
    }

    private void handleMemberDeath(Player player) {
        BetterTeamsIntegration integration = plugin.getBetterTeamsIntegration();
        if (integration == null || !integration.isEnabled()) return;

        // Cari team yang player ini masuk
        for (BetterTeamsIntegration.TeamData team : integration.readAllTeams().values()) {
            if (team.members.contains(player.getUniqueId())) {
                String clanColor = integration.convertColor(team.colorCode);
                org.bukkit.ChatColor color = dev.pterox.maphunter.util.ColorUtil.getChatColor(clanColor);
                
                Player killer = player.getKiller();
                String killerName = killer != null ? killer.getName() : null;
                
                String msg = color + "[" + team.name + "] &f" + player.getName() + " &7telah mati";
                if (killerName != null) {
                    msg += " &7oleh &c" + killerName;
                }
                
                for (Player online : Bukkit.getOnlinePlayers()) {
                    online.sendMessage(MessageUtil.color("&8[&b&lMapHunter&8] " + msg));
                }
                
                LogUtil.log("Member death: " + player.getName() + " (team: " + team.name + ") killed by " + (killerName != null ? killerName : "unknown"));
                return;
            }
        }
    }

    private boolean hasBackupMap(Player player) {
        for (ItemStack item : player.getInventory().getContents()) {
            if (ItemUtil.isBackupMap(item)) return true;
        }
        for (ItemStack item : player.getInventory().getArmorContents()) {
            if (ItemUtil.isBackupMap(item)) return true;
        }
        return false;
    }
}
