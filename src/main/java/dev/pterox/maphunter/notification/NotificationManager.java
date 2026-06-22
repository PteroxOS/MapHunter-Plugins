package dev.pterox.maphunter.notification;

import dev.pterox.maphunter.MapHunter;
import dev.pterox.maphunter.util.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class NotificationManager {

    private final MapHunter plugin;

    public NotificationManager(MapHunter plugin) {
        this.plugin = plugin;
    }

    private static final String PREFIX = "&8[&b&lMapHunter&8] &r";
    private static final String BORDER = "&8&m                              ";

    public void broadcastLeaderDeath(String clanName, String playerName) {
        broadcastLeaderDeath(clanName, playerName, null);
    }

    public void broadcastLeaderDeath(String clanName, String playerName, String killerName) {
        String titleTemplate = plugin.getMessageConfig().getConfig().getString("notification.leader-death-title", "&c&lLEADER TEREIMINASI");
        String subtitleTemplate = plugin.getMessageConfig().getConfig().getString("notification.leader-death-subtitle", "&c☠ Ketua clan &l{clan} &r&c({player}) telah terbunuh!");
        
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("clan", clanName);
        placeholders.put("player", playerName);
        
        String title = MessageUtil.format(titleTemplate, placeholders);
        String subtitle = MessageUtil.format(subtitleTemplate, placeholders);
        
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.sendTitle(title, subtitle, 10, 60, 20);
            player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 1.0f, 1.0f);
        }
        
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.sendMessage(MessageUtil.color(""));
            player.sendMessage(MessageUtil.color(BORDER));
            player.sendMessage(MessageUtil.color(PREFIX + "&c&l☠ LEADER TEREIMINASI"));
            player.sendMessage(MessageUtil.color(PREFIX + "&fClan: &e" + clanName + " &f| Pemain: &c" + playerName));
            if (killerName != null) {
                player.sendMessage(MessageUtil.color(PREFIX + "&fDibunuh oleh: &a" + killerName));
            }
            player.sendMessage(MessageUtil.color(BORDER));
            player.sendMessage(MessageUtil.color(""));
        }
    }

    public void broadcastEventStart() {
        String titleTemplate = plugin.getMessageConfig().getConfig().getString("notification.event-start-title", "&a&lMAP HUNTER");
        String subTitleTemplate = plugin.getMessageConfig().getConfig().getString("notification.event-start-subtitle", "&eEvent telah dimulai!");
        
        String title = MessageUtil.format(titleTemplate, null);
        String subtitle = MessageUtil.format(subTitleTemplate, null);
        
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.sendTitle(title, subtitle, 10, 70, 20);
            player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_ENDER_DRAGON_GROWL, 1.0f, 1.2f);
            
            try {
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
            }
            
            // Stylish chat
            player.sendMessage(MessageUtil.color(""));
            player.sendMessage(MessageUtil.color("&a&m                              "));
            player.sendMessage(MessageUtil.color(PREFIX + "&a&l⚔ EVENT DIMULAI ⚔"));
            player.sendMessage(MessageUtil.color(PREFIX + "&fSemua leader telah siap!"));
            player.sendMessage(MessageUtil.color(PREFIX + "&fGunakan &bHunter Map &funtuk melacak musuh."));
            player.sendMessage(MessageUtil.color("&a&m                              "));
            player.sendMessage(MessageUtil.color(""));
        }
    }

    public void broadcastEventStop() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.sendMessage(MessageUtil.color(""));
            player.sendMessage(MessageUtil.color("&c&m                              "));
            player.sendMessage(MessageUtil.color(PREFIX + "&c&l✗ EVENT BERAKHIR"));
            player.sendMessage(MessageUtil.color(PREFIX + "&fEvent MapHunter telah berakhir."));
            player.sendMessage(MessageUtil.color("&c&m                              "));
            player.sendMessage(MessageUtil.color(""));
        }
    }

    public void notifyBackupCountdownStart(Player backupPlayer, String clanName, int seconds) {
        String title = MessageUtil.color("&e&lBACKUP LEADER");
        String subtitle = MessageUtil.color("&eKamu adalah backup leader clan &b" + clanName);
        backupPlayer.sendTitle(title, subtitle, 10, 60, 20);
        
        backupPlayer.sendMessage(MessageUtil.color(""));
        backupPlayer.sendMessage(MessageUtil.color("&e&m                              "));
        backupPlayer.sendMessage(MessageUtil.color(PREFIX + "&e&l⚡ BACKUP LEADER"));
        backupPlayer.sendMessage(MessageUtil.color(PREFIX + "&fKamu adalah backup leader clan &b" + clanName));
        backupPlayer.sendMessage(MessageUtil.color(PREFIX + "&fCountdown: &c" + seconds + " detik"));
        backupPlayer.sendMessage(MessageUtil.color(PREFIX + "&fBersiap jika leader utama tidak kembali!"));
        backupPlayer.sendMessage(MessageUtil.color("&e&m                              "));
        backupPlayer.sendMessage(MessageUtil.color(""));
    }

    public void notifyBackupCountdownWarning(Player backupPlayer, String clanName, int secondsLeft) {
        String remaining;
        if (secondsLeft >= 60) {
            remaining = (secondsLeft / 60) + " menit";
        } else {
            remaining = secondsLeft + " detik";
        }
        String title = MessageUtil.color("&c&lSEGERA");
        String subtitle = MessageUtil.color("&eMap clan &b" + clanName + " &eakan dipindahkan ke kamu");
        backupPlayer.sendTitle(title, subtitle, 10, 40, 20);
        
        backupPlayer.sendMessage(MessageUtil.color(""));
        backupPlayer.sendMessage(MessageUtil.color("&c&m                              "));
        backupPlayer.sendMessage(MessageUtil.color(PREFIX + "&c&l⚠ PERINGATAN"));
        backupPlayer.sendMessage(MessageUtil.color(PREFIX + "&fMap clan &e" + clanName + " &fakan dipindahkan ke kamu dalam &c" + remaining));
        backupPlayer.sendMessage(MessageUtil.color(PREFIX + "&fLeader utama harus segera kembali!"));
        backupPlayer.sendMessage(MessageUtil.color("&c&m                              "));
        backupPlayer.sendMessage(MessageUtil.color(""));
    }

    public void notifyAdminMapTransferred(String clanName, String backupName) {
        String adminPermission = "maphunter.admin";
        for (Player admin : Bukkit.getOnlinePlayers()) {
            if (admin.hasPermission(adminPermission)) {
                admin.sendMessage(MessageUtil.color(""));
                admin.sendMessage(MessageUtil.color("&6&m                              "));
                admin.sendMessage(MessageUtil.color(PREFIX + "&6&l📋 ADMIN NOTIFICATION"));
                admin.sendMessage(MessageUtil.color(PREFIX + "&eMap clan &b" + clanName + " &etelah dipindahkan ke &b" + backupName));
                admin.sendMessage(MessageUtil.color(PREFIX + "&fGunakan &e/rmh leader restore " + clanName + " &funtuk mengembalikan."));
                admin.sendMessage(MessageUtil.color("&6&m                              "));
                admin.sendMessage(MessageUtil.color(""));
            }
        }
    }
}
