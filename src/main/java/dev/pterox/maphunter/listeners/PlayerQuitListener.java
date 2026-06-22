package dev.pterox.maphunter.listeners;

import dev.pterox.maphunter.event.EventManager;
import dev.pterox.maphunter.map.MapManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerQuitListener implements Listener {

    private final MapManager mapManager;
    private final EventManager eventManager;

    public PlayerQuitListener(MapManager mapManager, EventManager eventManager) {
        this.mapManager = mapManager;
        this.eventManager = eventManager;
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        if (!eventManager.isEventActive()) return;
        mapManager.handlePlayerQuit(event.getPlayer());
    }
}
