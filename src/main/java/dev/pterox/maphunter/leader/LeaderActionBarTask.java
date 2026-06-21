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
import java.util.UUID;

public class LeaderActionBarTask extends BukkitRunnable {

    private final MapHunter plugin;
    private final LeaderManager leaderManager;
    private final Map<String, Long> lastGlowTime = new HashMap<>();

    public LeaderActionBarTask(MapHunter plugin, LeaderManager leaderManager) {
        this.plugin = plugin;
        this.leaderManager = leaderManager;
    }

    @Override
    public void run() {
        int radarDistance = plugin.getConfig().getInt("features.radar.distance", 200);
        long glowIntervalMs = plugin.getConfig().getInt("features.glowing.interval-seconds", 600) * 1000L;
        long now = System.currentTimeMillis();

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (leaderManager.isLeader(player)) {
                LeaderData data = leaderManager.getLeaderData(player);
                if (data != null) {
                    String clanColorCode = getColorCode(data.getClanColor());

                    double nearestEnemyDistance = -1;
                    double nearestAllyDistance = -1;
                    String nearestAllyName = null;

                    for (Player target : Bukkit.getOnlinePlayers()) {
                        if (target.equals(player)) continue;
                        if (!target.getWorld().equals(player.getWorld())) continue;

                        if (leaderManager.isLeader(target)) {
                            LeaderData targetData = leaderManager.getLeaderData(target);
                            if (targetData == null) continue;

                            double distance = player.getLocation().distance(target.getLocation());

                            if (targetData.getClanName().equalsIgnoreCase(data.getClanName())) {
                                if (nearestAllyDistance == -1 || distance < nearestAllyDistance) {
                                    nearestAllyDistance = distance;
                                    nearestAllyName = target.getName();
                                }
                            } else {
                                if (nearestEnemyDistance == -1 || distance < nearestEnemyDistance) {
                                    nearestEnemyDistance = distance;
                                }

                                // Glowing Effect dengan interval
                                if (plugin.getConfig().getBoolean("features.glowing.enabled", true)) {
                                    String glowKey = player.getUniqueId() + ":" + target.getUniqueId();
                                    Long lastGlow = lastGlowTime.get(glowKey);
                                    boolean canGlow = lastGlow == null || (now - lastGlow) >= glowIntervalMs;

                                    if (canGlow && distance <= radarDistance) {
                                        target.addPotionEffect(new org.bukkit.potion.PotionEffect(org.bukkit.potion.PotionEffectType.GLOWING, 40, 0, false, false, true));
                                        player.addPotionEffect(new org.bukkit.potion.PotionEffect(org.bukkit.potion.PotionEffectType.GLOWING, 40, 0, false, false, true));
                                        lastGlowTime.put(glowKey, now);
                                    }
                                }
                            }
                        }
                    }

                    // Build action bar message
                    StringBuilder msg = new StringBuilder();
                    msg.append(clanColorCode).append("Leader ").append(data.getClanName()).append(" &8» ");

                    if (nearestEnemyDistance != -1 && nearestEnemyDistance <= radarDistance) {
                        int bars = 10 - (int) ((nearestEnemyDistance / radarDistance) * 10);
                        bars = Math.max(0, Math.min(10, bars));

                        String enemyColor = getDistanceColor(nearestEnemyDistance, radarDistance);

                        StringBuilder progressBar = new StringBuilder();
                        for (int i = 0; i < 10; i++) {
                            if (i < bars) {
                                progressBar.append(enemyColor).append("■");
                            } else {
                                progressBar.append("&7").append("□");
                            }
                        }

                        msg.append("&fMusuh: ").append(enemyColor).append("[").append(progressBar).append("] &7").append(String.format("%.0fm", nearestEnemyDistance));

                        if (nearestEnemyDistance <= 20) {
                            float pitch = 1.0f + (float) (1.0 - (nearestEnemyDistance / 20.0));
                            player.playSound(player.getLocation(), org.bukkit.Sound.BLOCK_NOTE_BLOCK_BASEDRUM, 1.0f, pitch);
                        }
                    } else if (nearestEnemyDistance != -1) {
                        msg.append("&fMusuh: &7[□□□□□□□□□□] &7>" + radarDistance + "m");
                    } else {
                        msg.append("&fMusuh: &aAman");
                    }

                    if (nearestAllyDistance != -1) {
                        msg.append(" &8| ").append(clanColorCode).append("Teman: &f").append(nearestAllyName).append(" &7").append(String.format("%.0fm", nearestAllyDistance));
                    }

                    String message = MessageUtil.color(msg.toString());

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

    private String getDistanceColor(double distance, int maxDistance) {
        double ratio = distance / maxDistance;
        if (ratio <= 0.2) return "&c";
        else if (ratio <= 0.4) return "&6";
        else if (ratio <= 0.6) return "&e";
        else if (ratio <= 0.8) return "&2";
        else return "&a";
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
