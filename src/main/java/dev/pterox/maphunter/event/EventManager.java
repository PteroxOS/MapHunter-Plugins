package dev.pterox.maphunter.event;

import dev.pterox.maphunter.MapHunter;
import dev.pterox.maphunter.map.MapManager;
import dev.pterox.maphunter.notification.NotificationManager;

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
                    org.bukkit.Bukkit.broadcastMessage(dev.pterox.maphunter.util.MessageUtil.color("&c&lWAKTU HABIS! &eEvent MapHunter telah berakhir."));
                    stopEvent();
                }
            }.runTaskLater(plugin, durationSeconds * 20L);
        }
    }

    public void triggerAutoWin(org.bukkit.entity.Player winner, String clanName) {
        if (!eventActive) return;
        org.bukkit.Bukkit.broadcastMessage(dev.pterox.maphunter.util.MessageUtil.color("&a&lSELAMAT! &eClan &b[" + clanName + "] &eberhasil memenangkan event MapHunter!"));
        
        // Fireworks
        org.bukkit.inventory.meta.FireworkMeta fm = (org.bukkit.inventory.meta.FireworkMeta) org.bukkit.Bukkit.getItemFactory().getItemMeta(org.bukkit.Material.FIREWORK_ROCKET);
        fm.addEffect(org.bukkit.FireworkEffect.builder().withColor(org.bukkit.Color.GREEN).with(org.bukkit.FireworkEffect.Type.BALL_LARGE).build());
        fm.setPower(1);
        for (int i = 0; i < 5; i++) {
            org.bukkit.Location loc = winner.getLocation().add(Math.random() * 4 - 2, 2, Math.random() * 4 - 2);
            org.bukkit.entity.Firework fw = loc.getWorld().spawn(loc, org.bukkit.entity.Firework.class);
            fw.setFireworkMeta(fm);
        }
        
        stopEvent();
    }

    public void stopEvent() {
        if (!eventActive) return;
        eventActive = false;
        
        if (timeLimitTask != null) {
            timeLimitTask.cancel();
            timeLimitTask = null;
        }
        
        mapManager.stopMapSchedules();
        mapManager.removeMapsFromAllLeaders();
        
        // Remove all leaders automatically when the event stops
        for (dev.pterox.maphunter.leader.LeaderData data : plugin.getLeaderManager().getAllLeaders()) {
            plugin.getLeaderManager().removeLeader(data.getUuid());
        }
        
        notificationManager.broadcastEventStop();
    }
}
