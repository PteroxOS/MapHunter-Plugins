package dev.pterox.maphunter.map;

import dev.pterox.maphunter.MapHunter;
import dev.pterox.maphunter.leader.LeaderData;
import dev.pterox.maphunter.leader.LeaderManager;
import dev.pterox.maphunter.notification.NotificationManager;
import dev.pterox.maphunter.util.ItemUtil;
import dev.pterox.maphunter.util.LogUtil;
import dev.pterox.maphunter.util.SchedulerUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.map.MapView;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class MapManager {

    private final MapHunter plugin;
    private final LeaderManager leaderManager;
    private final SchedulerUtil schedulerUtil;
    private NotificationManager notificationManager;
    
    private PlayerPositionHistory positionHistory;
    private BukkitTask positionRecordTask;
    private BukkitTask mapRenderTask;
    
    private final Map<UUID, Integer> backupMapIds = new HashMap<>();

    public MapManager(MapHunter plugin, LeaderManager leaderManager, SchedulerUtil schedulerUtil) {
        this.plugin = plugin;
        this.leaderManager = leaderManager;
        this.schedulerUtil = schedulerUtil;
    }

    public void setNotificationManager(NotificationManager notificationManager) {
        this.notificationManager = notificationManager;
    }

    public void init() {
        int delayTicks = plugin.getConfig().getInt("map.position-delay-ticks", 100);
        this.positionHistory = new PlayerPositionHistory(delayTicks);
    }

    public void startMapSchedules() {
        positionRecordTask = schedulerUtil.runTaskTimer(() -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                positionHistory.recordPosition(player.getUniqueId(), player.getLocation());
            }
        }, 1L, 1L);

        mapRenderTask = schedulerUtil.runTaskTimer(() -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                LeaderData data = leaderManager.getLeaderData(player);
                if (data != null && data.getMapId() != -1) {
                    @SuppressWarnings("deprecation")
                    MapView mapView = Bukkit.getMap(data.getMapId());
                    if (mapView != null && mapView.getWorld() != null && mapView.getWorld().equals(player.getWorld())) {
                        updateMapCenter(mapView, player);
                    }
                }
                // Update center untuk backup map juga
                Integer backupMapId = backupMapIds.get(player.getUniqueId());
                if (backupMapId != null && backupMapId != -1) {
                    @SuppressWarnings("deprecation")
                    MapView mapView = Bukkit.getMap(backupMapId);
                    if (mapView != null && mapView.getWorld() != null && mapView.getWorld().equals(player.getWorld())) {
                        updateMapCenter(mapView, player);
                    }
                }
            }
        }, 20L, 20L);
    }

    private void updateMapCenter(MapView mapView, Player player) {
        int scaleMultiplier = 1;
        switch (mapView.getScale()) {   
            case CLOSEST: scaleMultiplier = 1; break;
            case CLOSE: scaleMultiplier = 2; break;
            case NORMAL: scaleMultiplier = 4; break;
            case FAR: scaleMultiplier = 8; break;
            case FARTHEST: scaleMultiplier = 16; break;
        }
        
        int centerX = mapView.getCenterX();
        int centerZ = mapView.getCenterZ();
        int pX = player.getLocation().getBlockX();
        int pZ = player.getLocation().getBlockZ();
        
        int diameter = 128 * scaleMultiplier;
        int radius = 64 * scaleMultiplier;
        
        int newCenterX = centerX;
        int newCenterZ = centerZ;
        
        while (pX >= newCenterX + radius) newCenterX += diameter;
        while (pX < newCenterX - radius) newCenterX -= diameter;
        while (pZ >= newCenterZ + radius) newCenterZ += diameter;
        while (pZ < newCenterZ - radius) newCenterZ -= diameter;
        
        if (newCenterX != centerX || newCenterZ != centerZ) {
            mapView.setCenterX(newCenterX);
            mapView.setCenterZ(newCenterZ);
        }
    }

    public void stopMapSchedules() {
        if (positionRecordTask != null) {
            positionRecordTask.cancel();
            positionRecordTask = null;
        }
        if (mapRenderTask != null) {
            mapRenderTask.cancel();
            mapRenderTask = null;
        }
        positionHistory.clearAll();
    }

    public void createHunterMap(Player leader) {
        LeaderData data = leaderManager.getLeaderData(leader);
        if (data == null) return;
        createHunterMapInternal(leader, data, false);
    }

    public void createBackupMap(Player holder, LeaderData data) {
        createHunterMapInternal(holder, data, true);
    }

    private void createHunterMapInternal(Player holder, LeaderData data, boolean isBackup) {
        World world = holder.getWorld();
        MapView mapView = Bukkit.createMap(world);
        
        String scaleStr = plugin.getConfig().getString("map.scale", "NORMAL");
        try {
            mapView.setScale(MapView.Scale.valueOf(scaleStr.toUpperCase()));
        } catch (IllegalArgumentException e) {
            mapView.setScale(MapView.Scale.NORMAL);
        }
        
        mapView.setTrackingPosition(false);
        mapView.setUnlimitedTracking(true);
        
        mapView.setCenterX(holder.getLocation().getBlockX());
        mapView.setCenterZ(holder.getLocation().getBlockZ());
        
        mapView.addRenderer(new HunterMapRenderer(positionHistory, leaderManager));

        int mapId = mapView.getId();

        ItemStack mapItem = new ItemStack(Material.FILLED_MAP);
        MapMeta meta = (MapMeta) mapItem.getItemMeta();
        if (meta != null) {
            meta.setMapView(mapView);
            if (isBackup) {
                meta.setDisplayName("§e§lBackup Leader Map");
                meta.getPersistentDataContainer().set(ItemUtil.getBackupMapKey(), PersistentDataType.BYTE, (byte) 1);
                backupMapIds.put(holder.getUniqueId(), mapId);
                // Pastikan backup terdaftar sebagai leader agar auto-win bisa jalan
                if (!leaderManager.isLeader(holder)) {
                    leaderManager.addLeader(holder, data.getClanName(), data.getClanColor());
                }
            } else {
                meta.setDisplayName("§b§lLeader Map");
                meta.getPersistentDataContainer().set(ItemUtil.getMapKey(), PersistentDataType.BYTE, (byte) 1);
                data.setMapId(mapId);
                leaderManager.saveLeader(data);
            }
            meta.setCustomModelData(999);
            mapItem.setItemMeta(meta);
        }

        holder.getInventory().addItem(mapItem);
    }

    public void removeHunterMap(Player leader) {
        LeaderData data = leaderManager.getLeaderData(leader);
        if (data != null) {
            data.setMapId(-1);
            leaderManager.saveToDbOnly(data);
        }
        
        for (int i = 0; i < leader.getInventory().getSize(); i++) {
            ItemStack item = leader.getInventory().getItem(i);
            if (ItemUtil.isHunterMap(item)) {
                leader.getInventory().setItem(i, null);
            }
        }
    }

    public void removeBackupMap(Player holder) {
        backupMapIds.remove(holder.getUniqueId());
        
        for (int i = 0; i < holder.getInventory().getSize(); i++) {
            ItemStack item = holder.getInventory().getItem(i);
            if (ItemUtil.isBackupMap(item)) {
                holder.getInventory().setItem(i, null);
            }
        }
    }

    public void giveMapsToAllLeaders() {
        List<LeaderData> leaders = leaderManager.getAllLeaders();
        for (LeaderData data : leaders) {
            Player p = Bukkit.getPlayer(data.getUuid());
            if (p != null && p.isOnline()) {
                createHunterMap(p);
            }
        }
    }

    public void removeMapsFromAllLeaders() {
        List<LeaderData> leaders = leaderManager.getAllLeaders();
        for (LeaderData data : leaders) {
            Player p = Bukkit.getPlayer(data.getUuid());
            if (p != null && p.isOnline()) {
                removeHunterMap(p);
            }
        }
        // Clear all backup maps
        for (Map.Entry<UUID, Integer> entry : backupMapIds.entrySet()) {
            Player p = Bukkit.getPlayer(entry.getKey());
            if (p != null && p.isOnline()) {
                removeBackupMap(p);
            }
        }
        backupMapIds.clear();
    }

    public void handlePlayerQuit(Player p) {
        UUID uuid = p.getUniqueId();
        positionHistory.clearHistory(uuid);
        
        LeaderData leaderData = leaderManager.getLeaderData(uuid);
        if (leaderData != null) {
            removeHunterMap(p);
            
            // Hapus leader utama dari cache (bukan DB)
            leaderManager.removeFromCacheOnly(uuid);
            LogUtil.logRemoveLeader(p.getName(), leaderData.getClanName() + " (digantikan backup)");
            
            // Pindahkan map ke backup jika online
            if (leaderData.getBackupUuid() != null) {
                Player backup = Bukkit.getPlayer(leaderData.getBackupUuid());
                if (backup != null && backup.isOnline()) {
                    leaderManager.addLeader(backup, leaderData.getClanName(), leaderData.getClanColor());
                    
                    createBackupMap(backup, leaderData);
                    LogUtil.logMapTransfer(p.getName(), backup.getName(), leaderData.getClanName());
                    LogUtil.logAddBackup(backup.getName(), leaderData.getClanName());
                    
                    // Info stylish
                    Bukkit.broadcastMessage(dev.pterox.maphunter.util.MessageUtil.color(""));
                    Bukkit.broadcastMessage(dev.pterox.maphunter.util.MessageUtil.color("&e&m                              "));
                    Bukkit.broadcastMessage(dev.pterox.maphunter.util.MessageUtil.color("&8[&b&lMapHunter&8] &r&e&l⚡ LEADER OFFLINE"));
                    Bukkit.broadcastMessage(dev.pterox.maphunter.util.MessageUtil.color("&8[&b&lMapHunter&8] &r&fLeader &e" + p.getName() + " &f(clan &e" + leaderData.getClanName() + "&f) offline."));
                    Bukkit.broadcastMessage(dev.pterox.maphunter.util.MessageUtil.color("&8[&b&lMapHunter&8] &r&fMap dipindahkan ke &b" + backup.getName()));
                    Bukkit.broadcastMessage(dev.pterox.maphunter.util.MessageUtil.color("&8[&b&lMapHunter&8] &r&7Leader utama harus lapor admin untuk mengembalikan map."));
                    Bukkit.broadcastMessage(dev.pterox.maphunter.util.MessageUtil.color("&e&m                              "));
                    Bukkit.broadcastMessage(dev.pterox.maphunter.util.MessageUtil.color(""));
                    
                    String title = dev.pterox.maphunter.util.MessageUtil.color("&e&lMAP DITERIMA");
                    String subtitle = dev.pterox.maphunter.util.MessageUtil.color("&eKamu menggantikan leader utama clan &b" + leaderData.getClanName());
                    backup.sendTitle(title, subtitle, 10, 60, 20);
                }
            }
        }
    }

    public void handlePlayerJoin(Player p) {
        UUID uuid = p.getUniqueId();
        LeaderData leaderData = leaderManager.getLeaderData(uuid);
        
        if (leaderData == null) return;
        
        String clanName = leaderData.getClanName();
        
        // Leader utama join tapi sudah digantikan backup
        p.sendMessage(dev.pterox.maphunter.util.MessageUtil.color(""));
        p.sendMessage(dev.pterox.maphunter.util.MessageUtil.color("&e&m                              "));
        p.sendMessage(dev.pterox.maphunter.util.MessageUtil.color("&8[&b&lMapHunter&8] &r&e&l⚠ KAMU SUDAH TIDAK MENJABAT"));
        p.sendMessage(dev.pterox.maphunter.util.MessageUtil.color("&8[&b&lMapHunter&8] &r&fJabatan leader clan &e" + clanName + " &ftelah digantikan."));
        p.sendMessage(dev.pterox.maphunter.util.MessageUtil.color("&8[&b&lMapHunter&8] &r&cLapor admin untuk mengembalikan map."));
        p.sendMessage(dev.pterox.maphunter.util.MessageUtil.color("&e&m                              "));
        p.sendMessage(dev.pterox.maphunter.util.MessageUtil.color(""));
    }

    public void restoreLeader(Player leader) {
        LeaderData data = leaderManager.getLeaderData(leader);
        if (data == null) return;
        
        if (data.getBackupUuid() != null) {
            Player backup = Bukkit.getPlayer(data.getBackupUuid());
            if (backup != null && backup.isOnline()) {
                removeBackupMap(backup);
            }
            leaderManager.removeLeader(data.getBackupUuid());
        }
        
        data.setReplacedByBackup(false);
        leaderManager.saveLeader(data);
        
        createHunterMap(leader);
    }

    public boolean hasBackupMapTracked(UUID uuid) {
        return backupMapIds.containsKey(uuid);
    }
}
