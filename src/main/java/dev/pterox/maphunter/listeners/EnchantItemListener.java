package dev.pterox.maphunter.listeners;

import dev.pterox.maphunter.event.EventManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.enchantment.EnchantItemEvent;

public class EnchantItemListener implements Listener {

    private final EventManager eventManager;

    public EnchantItemListener(EventManager eventManager) {
        this.eventManager = eventManager;
    }

    @EventHandler
    public void onEnchantItem(EnchantItemEvent event) {
        if (eventManager.isEventActive()) {
            event.setCancelled(true);
        }
    }
}
