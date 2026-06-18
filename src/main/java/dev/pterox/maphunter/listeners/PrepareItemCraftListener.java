package dev.pterox.maphunter.listeners;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.inventory.ItemStack;

public class PrepareItemCraftListener implements Listener {

    @EventHandler
    public void onPrepareItemCraft(PrepareItemCraftEvent event) {
        if (event.getRecipe() != null) {
            ItemStack result = event.getRecipe().getResult();
            if (result.getType() == Material.FILLED_MAP || result.getType() == Material.MAP) {
                event.getInventory().setResult(new ItemStack(Material.AIR));
            }
        }
    }
}
