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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class MapManager {

    private final MapHunter plugin;
    private final LeaderManager leaderManager;
    private final SchedulerUtil schedulerUtil;
    
    private PlayerPositionHistory positionHistory;
    private BukkitTask positionRecordTask;
    private BukkitTask mapRenderTask;
    
    private final Map<String, BukkitTask> countdownTasks = new HashMap<>();

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
                    @SuppressWarnings("deprecation")
                    MapView mapView = Bukkit.getMap(data.getMapId());
                    if (mapView != null && mapView.getWorld().equals(player.getWorld())) {
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
                        
                        // Geser grid map persis sebesar diameter jika player melewati batas radius
                        while (pX >= newCenterX + radius) {
                            newCenterX += diameter;
                        }
                        while (pX < newCenterX - radius) {
                            newCenterX -= diameter;
                        }
                        
                        while (pZ >= newCenterZ + radius) {
                            newCenterZ += diameter;
                        }
                        while (pZ < newCenterZ - radius) {
                            newCenterZ -= diameter;
                        }
                        
                        if (newCenterX != centerX || newCenterZ != centerZ) {
                            mapView.setCenterX(newCenterX);
                            mapView.setCenterZ(newCenterZ);
                        }
                    }
                }
            }
        }, 20L, 20L); // Cek tiap detik, bukan pakai updateInterval lambat
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
        mapView.setUnlimitedTracking(true);
        
        int scaleMultiplier = 1;
        switch (mapView.getScale()) {   
            case CLOSEST: scaleMultiplier = 1; break;
            case CLOSE: scaleMultiplier = 2; break;
            case NORMAL: scaleMultiplier = 4; break;
            case FAR: scaleMultiplier = 8; break;
            case FARTHEST: scaleMultiplier = 16; break;
        }
        int pX = leader.getLocation().getBlockX();
        int pZ = leader.getLocation().getBlockZ();
        
        mapView.setCenterX(pX);
        mapView.setCenterZ(pZ);
        // Do not remove existing renderers so the vanilla map terrain still renders
        
        // Add our custom renderer to draw the cursors on top of the terrain
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
        
        for (int i = 0; i < leader.getInventory().getSize(); i++) {
            ItemStack item = leader.getInventory().getItem(i);
            if (ItemUtil.isHunterMap(item)) {
                leader.getInventory().setItem(i, null);
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

    public void handlePlayerQuit(Player p) {
        UUID uuid = p.getUniqueId();
        positionHistory.clearHistory(uuid);
        
        LeaderData leaderData = leaderManager.getLeaderData(uuid);
        if (leaderData != null) {
            // Leader yang quit
            removeHunterMap(p);
            
            // Cek backup leader
            if (leaderData.getBackupUuid() != null) {
                Player backup = Bukkit.getPlayer(leaderData.getBackupUuid());
                if (backup != null && backup.isOnline()) {
                    createHunterMap(backup);
                    Bukkit.broadcastMessage(dev.pterox.maphunter.util.MessageUtil.color("&e[MapHunter] &fMap " + leaderData.getClanName() + " dipindahkan ke leader cadangan &b" + backup.getName()));
                    return; // Selesai
                }
            }
            
            // Tidak ada backup leader atau sedang offline -> mulai countdown
            int countdownSeconds = plugin.getConfig().getInt("event.leader-offline-countdown", 60);
            Bukkit.broadcastMessage(dev.pterox.maphunter.util.MessageUtil.color("&c[MapHunter] &fLeader " + leaderData.getClanName() + " offline dan tidak ada backup! Memulai countdown &e" + countdownSeconds + " detik&f..."));
            
            BukkitTask task = schedulerUtil.runTaskTimer(new Runnable() {
                int timeLeft = countdownSeconds;

                @Override
                public void run() {
                    // Cek apakah leader sudah online atau backup sudah online
                    boolean isLeaderOnline = Bukkit.getPlayer(uuid) != null;
                    boolean isBackupOnline = leaderData.getBackupUuid() != null && Bukkit.getPlayer(leaderData.getBackupUuid()) != null;
                    
                    if (isLeaderOnline || isBackupOnline) {
                        Bukkit.broadcastMessage(dev.pterox.maphunter.util.MessageUtil.color("&a[MapHunter] &fCountdown untuk " + leaderData.getClanName() + " dibatalkan karena leader/backup telah online."));
                        countdownTasks.remove(leaderData.getClanName()).cancel();
                        if (isLeaderOnline) {
                            createHunterMap(Bukkit.getPlayer(uuid));
                        } else {
                            createHunterMap(Bukkit.getPlayer(leaderData.getBackupUuid()));
                        }
                        return;
                    }
                    
                    if (timeLeft <= 0) {
                        Bukkit.broadcastMessage(dev.pterox.maphunter.util.MessageUtil.color("&c[MapHunter] &fWaktu habis! Clan &e" + leaderData.getClanName() + " &ftelah kalah karena tidak ada leader!"));
                        leaderManager.removeLeader(uuid);
                        countdownTasks.remove(leaderData.getClanName()).cancel();
                        return;
                    }
                    
                    if (timeLeft % 10 == 0 || timeLeft <= 5) {
                        Bukkit.broadcastMessage(dev.pterox.maphunter.util.MessageUtil.color("&e[MapHunter] &f" + leaderData.getClanName() + " akan kalah dalam &c" + timeLeft + " detik&f!"));
                    }
                    
                    timeLeft--;
                }
            }, 0L, 20L);
            
            countdownTasks.put(leaderData.getClanName(), task);
        }
    }
}
