package dev.pterox.maphunter.listeners;

import dev.pterox.maphunter.map.MapManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerQuitListener implements Listener {

    private final MapManager mapManager;

    public PlayerQuitListener(MapManager mapManager) {
        this.mapManager = mapManager;
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        mapManager.handlePlayerQuit(event.getPlayer());
    }
}
