package dev.pterox.maphunter.leader;

import dev.pterox.maphunter.MapHunter;
import dev.pterox.maphunter.util.MessageUtil;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

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
                    
                    double nearestDistance = -1;
                    for (Player target : Bukkit.getOnlinePlayers()) {
                        if (target.equals(player)) continue;
                        if (!target.getWorld().equals(player.getWorld())) continue;
                        
                        if (leaderManager.isLeader(target)) {
                            LeaderData targetData = leaderManager.getLeaderData(target);
                            if (targetData != null && !targetData.getClanName().equalsIgnoreCase(data.getClanName())) {
                                double distance = player.getLocation().distance(target.getLocation());
                                if (nearestDistance == -1 || distance < nearestDistance) {
                                    nearestDistance = distance;
                                }
                                
                                // Glowing Effect (Anti-Ngendok)
                                if (plugin.getConfig().getBoolean("features.glowing.enabled", true)) {
                                    int glowDist = plugin.getConfig().getInt("features.glowing.distance", 10);
                                    if (distance <= glowDist) {
                                        // Berikan glow ke target dan player
                                        target.addPotionEffect(new org.bukkit.potion.PotionEffect(org.bukkit.potion.PotionEffectType.GLOWING, 40, 0, false, false, true));
                                        player.addPotionEffect(new org.bukkit.potion.PotionEffect(org.bukkit.potion.PotionEffectType.GLOWING, 40, 0, false, false, true));
                                    }
                                }
                            }
                        }
                    }

                    int maxDistance = 200; // Maksimal jarak radar
                    int bars = 0;
                    String distanceText;
                    
                    if (nearestDistance != -1) {
                        if (nearestDistance <= maxDistance) {
                            bars = 10 - (int) ((nearestDistance / maxDistance) * 10);
                            bars = Math.max(0, Math.min(10, bars));
                            distanceText = String.format("%.1fm", nearestDistance);
                            
                            // Heartbeat SFX if close
                            if (nearestDistance <= 20) {
                                float pitch = 1.0f + (float) (1.0 - (nearestDistance / 20.0)); // pitch 1.0 to 2.0
                                player.playSound(player.getLocation(), org.bukkit.Sound.BLOCK_NOTE_BLOCK_BASEDRUM, 1.0f, pitch);
                            }
                        } else {
                            distanceText = ">200m";
                        }
                    } else {
                        distanceText = "Aman";
                    }

                    StringBuilder progressBar = new StringBuilder();
                    for (int i = 0; i < 10; i++) {
                        if (i < bars) {
                            progressBar.append("■");
                        } else {
                            progressBar.append("□");
                        }
                    }

                    String message = colorCode + "Leader " + data.getClanName() + " &8» &fJarak Musuh: " + colorCode + "[" + progressBar.toString() + "] &7" + distanceText;
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
