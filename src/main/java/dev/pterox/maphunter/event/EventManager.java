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

    public void startEvent() {
        if (eventActive) return;
        eventActive = true;
        
        mapManager.startMapSchedules();
        mapManager.giveMapsToAllLeaders();
        
        notificationManager.broadcastEventStart();
    }

    public void stopEvent() {
        if (!eventActive) return;
        eventActive = false;
        
        mapManager.stopMapSchedules();
        mapManager.removeMapsFromAllLeaders();
        
        // Remove all leaders automatically when the event stops
        for (dev.pterox.maphunter.leader.LeaderData data : plugin.getLeaderManager().getAllLeaders()) {
            plugin.getLeaderManager().removeLeader(data.getUuid());
        }
        
        notificationManager.broadcastEventStop();
    }
}
