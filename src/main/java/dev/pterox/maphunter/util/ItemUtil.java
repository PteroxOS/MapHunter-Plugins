package dev.pterox.maphunter.util;

import dev.pterox.maphunter.MapHunter;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public class ItemUtil {

    private static NamespacedKey mapKey;
    private static NamespacedKey backupMapKey;

    public static void init(MapHunter plugin) {
        mapKey = new NamespacedKey(plugin, "leader_map");
        backupMapKey = new NamespacedKey(plugin, "backup_leader_map");
    }

    public static NamespacedKey getMapKey() {
        return mapKey;
    }

    public static NamespacedKey getBackupMapKey() {
        return backupMapKey;
    }

    public static boolean isHunterMap(ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return false;
        }
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return false;
        }
        return meta.getPersistentDataContainer().has(mapKey, PersistentDataType.BYTE)
            || meta.getPersistentDataContainer().has(backupMapKey, PersistentDataType.BYTE);
    }

    public static boolean isBackupMap(ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return false;
        }
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return false;
        }
        return meta.getPersistentDataContainer().has(backupMapKey, PersistentDataType.BYTE);
    }
}
