package dev.pterox.maphunter.map;

import dev.pterox.maphunter.MapHunter;
import dev.pterox.maphunter.leader.LeaderData;
import dev.pterox.maphunter.leader.LeaderManager;
import dev.pterox.maphunter.util.ItemUtil;
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

import java.util.List;
import java.util.UUID;

public class MapManager {

    private final MapHunter plugin;
    private final LeaderManager leaderManager;
    private final SchedulerUtil schedulerUtil;
    
    private PlayerPositionHistory positionHistory;
    private BukkitTask positionRecordTask;
    private BukkitTask mapRenderTask;

    public MapManager(MapHunter plugin, LeaderManager leaderManager, SchedulerUtil schedulerUtil) {
        this.plugin = plugin;
        this.leaderManager = leaderManager;
        this.schedulerUtil = schedulerUtil;
    }

    public void init() {
        int delayTicks = plugin.getConfig().getInt("map.position-delay-ticks", 100);
        // We record every tick, so delayTicks = max capacity
        this.positionHistory = new PlayerPositionHistory(delayTicks);
    }

    public void startMapSchedules() {
        int updateInterval = plugin.getConfig().getInt("map.update-interval-ticks", 100);

        positionRecordTask = schedulerUtil.runTaskTimer(() -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                positionHistory.recordPosition(player.getUniqueId(), player.getLocation());
            }
        }, 1L, 1L);

        mapRenderTask = schedulerUtil.runTaskTimer(() -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                LeaderData data = leaderManager.getLeaderData(player);
                if (data != null && data.getMapId() != -1) {
                    // Force update on map id
                    @SuppressWarnings("deprecation")
                    MapView view = Bukkit.getMap(data.getMapId());
                    if (view != null) {
                        // Re-center on player
                        view.setCenterX(player.getLocation().getBlockX());
                        view.setCenterZ(player.getLocation().getBlockZ());
                        view.setWorld(player.getWorld());
                        
                        // Force render by sending packet or just letting the natural tick pick up
                        // The custom renderer will handle it.
                    }
                }
            }
        }, updateInterval, updateInterval);
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

        World world = leader.getWorld();
        MapView mapView = Bukkit.createMap(world);
        
        String scaleStr = plugin.getConfig().getString("map.scale", "NORMAL");
        try {
            mapView.setScale(MapView.Scale.valueOf(scaleStr.toUpperCase()));
        } catch (IllegalArgumentException e) {
            mapView.setScale(MapView.Scale.NORMAL);
        }
        
        mapView.setTrackingPosition(false);
        mapView.getRenderers().clear();
        mapView.addRenderer(new HunterMapRenderer(positionHistory, leaderManager));

        int mapId = mapView.getId();
        data.setMapId(mapId);
        leaderManager.saveLeader(data); // update DB

        ItemStack mapItem = new ItemStack(Material.FILLED_MAP);
        MapMeta meta = (MapMeta) mapItem.getItemMeta();
        if (meta != null) {
            meta.setMapView(mapView);
            meta.setDisplayName("§b§lHunter Map");
            meta.getPersistentDataContainer().set(ItemUtil.getMapKey(), PersistentDataType.BYTE, (byte) 1);
            meta.setCustomModelData(999); // optional
            mapItem.setItemMeta(meta);
        }

        leader.getInventory().addItem(mapItem);
    }

    public void removeHunterMap(Player leader) {
        LeaderData data = leaderManager.getLeaderData(leader);
        if (data != null) {
            data.setMapId(-1);
            leaderManager.saveLeader(data);
        }
        
        for (ItemStack item : leader.getInventory().getContents()) {
            if (ItemUtil.isHunterMap(item)) {
                leader.getInventory().remove(item);
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
    }

    public void handlePlayerQuit(UUID uuid) {
        positionHistory.clearHistory(uuid);
    }
}
