package dev.pterox.maphunter.listeners;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.VillagerAcquireTradeEvent;
import org.bukkit.inventory.ItemStack;

public class VillagerTradeListener implements Listener {

    @EventHandler
    public void onVillagerAcquireTrade(VillagerAcquireTradeEvent event) {
        ItemStack result = event.getRecipe().getResult();
        if (result.getType() == Material.FILLED_MAP || result.getType() == Material.MAP) {
            event.setCancelled(true);
        }
    }
}
