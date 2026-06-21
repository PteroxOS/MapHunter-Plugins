package dev.pterox.maphunter.listeners;

import dev.pterox.maphunter.map.MapManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoinListener implements Listener {

    private final MapManager mapManager;

    public PlayerJoinListener(MapManager mapManager) {
        this.mapManager = mapManager;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        mapManager.handlePlayerJoin(event.getPlayer());
    }
}
