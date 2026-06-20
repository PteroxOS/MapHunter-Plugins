package dev.pterox.maphunter.listeners;

import dev.pterox.maphunter.util.ItemUtil;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class InventoryClickListener implements Listener {

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getClickedInventory() == null) return;

        ItemStack currentItem = event.getCurrentItem();
        ItemStack cursorItem = event.getCursor();
        
        boolean isCurrentMap = currentItem != null && ItemUtil.isHunterMap(currentItem);
        boolean isCursorMap = cursorItem != null && ItemUtil.isHunterMap(cursorItem);
        
        // Also check hotbar swap
        boolean isHotbarSwapMap = false;
        if (event.getClick() == ClickType.NUMBER_KEY) {
            ItemStack hotbarItem = event.getWhoClicked().getInventory().getItem(event.getHotbarButton());
            isHotbarSwapMap = hotbarItem != null && ItemUtil.isHunterMap(hotbarItem);
        }

        if (!isCurrentMap && !isCursorMap && !isHotbarSwapMap) return;

        Inventory topInv = event.getView().getTopInventory();
        Inventory bottomInv = event.getView().getBottomInventory();

        // If the top inventory is just their own crafting grid, it's fine
        if (topInv.getType() == InventoryType.CRAFTING) {
            return; 
        }

        // If they shift click the map from their inventory to the top inventory
        if (event.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY && event.getClickedInventory().equals(bottomInv)) {
            if (isCurrentMap) {
                event.setCancelled(true);
                event.getWhoClicked().sendMessage(dev.pterox.maphunter.util.MessageUtil.color("&cKamu tidak bisa memindahkan Map Radar keluar dari inventory!"));
            }
            return;
        }

        // If they click on the top inventory and they have the map on their cursor, or swap with number key
        if (event.getClickedInventory().equals(topInv)) {
            if (isCursorMap || isCurrentMap || isHotbarSwapMap) {
                event.setCancelled(true);
                event.getWhoClicked().sendMessage(dev.pterox.maphunter.util.MessageUtil.color("&cKamu tidak bisa memindahkan Map Radar keluar dari inventory!"));
            }
        }
    }
}
