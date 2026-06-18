package dev.pterox.maphunter.listeners;

import dev.pterox.maphunter.util.ItemUtil;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;

public class PlayerDropItemListener implements Listener {

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        if (ItemUtil.isHunterMap(event.getItemDrop().getItemStack())) {
            event.setCancelled(true);
        }
    }
}
