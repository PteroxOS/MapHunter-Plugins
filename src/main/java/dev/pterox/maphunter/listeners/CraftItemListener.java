package dev.pterox.maphunter.listeners;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.CraftItemEvent;

public class CraftItemListener implements Listener {

    @EventHandler
    public void onCraftItem(CraftItemEvent event) {
        if (event.getRecipe().getResult().getType() == Material.FILLED_MAP || 
            event.getRecipe().getResult().getType() == Material.MAP) {
            event.setCancelled(true);
        }
    }
}
