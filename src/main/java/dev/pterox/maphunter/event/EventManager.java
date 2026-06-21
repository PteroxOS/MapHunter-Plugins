package dev.pterox.maphunter.event;

import dev.pterox.maphunter.MapHunter;
import dev.pterox.maphunter.map.MapManager;
import dev.pterox.maphunter.notification.NotificationManager;

import java.util.ArrayList;
import java.util.List;

public class EventManager {

    private final MapHunter plugin;
    private final MapManager mapManager;
    private final NotificationManager notificationManager;
    
    private boolean eventActive = false;

    public EventManager(MapHunter plugin, MapManager mapManager, NotificationManager notificationManager) {
        this.plugin = plugin;
        this.mapManager = mapManager;
        this.notificationManager = notificationManager;
    }

    public boolean isEventActive() {
        return eventActive;
    }

    private org.bukkit.scheduler.BukkitTask timeLimitTask = null;

    public void startEvent() {
        if (eventActive) return;
        eventActive = true;
        
        mapManager.startMapSchedules();
        mapManager.giveMapsToAllLeaders();
        
        notificationManager.broadcastEventStart();
        
        // Start Time Limit Task
        if (plugin.getConfig().getBoolean("features.time-limit.enabled", true)) {
            int durationSeconds = plugin.getConfig().getInt("features.time-limit.duration-seconds", 3600);
            timeLimitTask = new org.bukkit.scheduler.BukkitRunnable() {
                @Override
                public void run() {
                    for (org.bukkit.entity.Player online : org.bukkit.Bukkit.getOnlinePlayers()) {
                        online.sendMessage(dev.pterox.maphunter.util.MessageUtil.color(""));
                        online.sendMessage(dev.pterox.maphunter.util.MessageUtil.color("&c&m                              "));
                        online.sendMessage(dev.pterox.maphunter.util.MessageUtil.color("&8[&b&lMapHunter&8] &r&c&l⏱ WAKTU HABIS"));
                        online.sendMessage(dev.pterox.maphunter.util.MessageUtil.color("&8[&b&lMapHunter&8] &r&fEvent MapHunter telah berakhir."));
                        online.sendMessage(dev.pterox.maphunter.util.MessageUtil.color("&c&m                              "));
                        online.sendMessage(dev.pterox.maphunter.util.MessageUtil.color(""));
                    }
                    stopEvent();
                }
            }.runTaskLater(plugin, durationSeconds * 20L);
        }
    }

    public void triggerAutoWin(org.bukkit.entity.Player winner, String clanName) {
        if (!eventActive) return;
        
        // Title congratulation
        String title = dev.pterox.maphunter.util.MessageUtil.color("&a&lSELAMAT!");
        String subtitle = dev.pterox.maphunter.util.MessageUtil.color("&eClan &b[" + clanName + "] &eberhasil memenangkan event MapHunter!");
        for (org.bukkit.entity.Player online : org.bukkit.Bukkit.getOnlinePlayers()) {
            online.sendTitle(title, subtitle, 10, 80, 20);
        }
        
        // Fireworks selama beberapa detik
        int fireworksDuration = plugin.getConfig().getInt("features.win-fireworks.duration-seconds", 5);
        int fireworksPerSecond = plugin.getConfig().getInt("features.win-fireworks.fireworks-per-second", 3);
        
        org.bukkit.scheduler.BukkitTask fireworksTask = new org.bukkit.scheduler.BukkitRunnable() {
            int ticks = 0;
            int maxTicks = fireworksDuration * 20;
            
            @Override
            public void run() {
                if (ticks >= maxTicks || winner == null || !winner.isOnline()) {
                    this.cancel();
                    return;
                }
                
                for (int i = 0; i < fireworksPerSecond; i++) {
                    org.bukkit.inventory.meta.FireworkMeta fm = (org.bukkit.inventory.meta.FireworkMeta) org.bukkit.Bukkit.getItemFactory().getItemMeta(org.bukkit.Material.FIREWORK_ROCKET);
                    org.bukkit.FireworkEffect.Builder effectBuilder = org.bukkit.FireworkEffect.builder()
                        .withColor(org.bukkit.Color.GREEN, org.bukkit.Color.YELLOW)
                        .with(org.bukkit.FireworkEffect.Type.BALL_LARGE)
                        .withFade(org.bukkit.Color.AQUA);
                    fm.addEffect(effectBuilder.build());
                    fm.setPower(1);
                    
                    org.bukkit.Location loc = winner.getLocation().add(Math.random() * 6 - 3, 2 + Math.random() * 3, Math.random() * 6 - 3);
                    org.bukkit.entity.Firework fw = loc.getWorld().spawn(loc, org.bukkit.entity.Firework.class);
                    fw.setFireworkMeta(fm);
                }
                
                ticks += 20;
            }
        }.runTaskTimer(plugin, 0L, 20L);
        
        // Stop event setelah fireworks selesai
        new org.bukkit.scheduler.BukkitRunnable() {
            @Override
            public void run() {
                stopEvent();
            }
        }.runTaskLater(plugin, (long) fireworksDuration * 20L + 40L);
    }

    public void stopEvent() {
        if (!eventActive) return;
        eventActive = false;
        
        if (timeLimitTask != null) {
            timeLimitTask.cancel();
            timeLimitTask = null;
        }
        
        mapManager.stopMapSchedules();
        
        try {
            mapManager.removeMapsFromAllLeaders();
        } catch (Exception e) {
            plugin.getLogger().warning("Error removing maps: " + e.getMessage());
        }
        
        try {
            List<dev.pterox.maphunter.leader.LeaderData> leaders = new ArrayList<>(plugin.getLeaderManager().getAllLeaders());
            for (dev.pterox.maphunter.leader.LeaderData data : leaders) {
                plugin.getLeaderManager().removeLeader(data.getUuid());
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Error removing leaders: " + e.getMessage());
        }
        
        notificationManager.broadcastEventStop();
    }
}
