package dev.pterox.maphunter.util;

import dev.pterox.maphunter.MapHunter;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public class ItemUtil {

    private static NamespacedKey mapKey;

    public static void init(MapHunter plugin) {
        mapKey = new NamespacedKey(plugin, "leader_map");
    }

    public static NamespacedKey getMapKey() {
        return mapKey;
    }

    public static boolean isHunterMap(ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return false;
        }
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return false;
        }
        return meta.getPersistentDataContainer().has(mapKey, PersistentDataType.BYTE);
    }
}
