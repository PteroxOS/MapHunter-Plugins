package dev.pterox.maphunter.map;

import dev.pterox.maphunter.MapHunter;
import dev.pterox.maphunter.leader.LeaderData;
import dev.pterox.maphunter.leader.LeaderManager;
import dev.pterox.maphunter.util.ColorUtil;
import dev.pterox.maphunter.util.ItemUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapCursor;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;

public class HunterMapRenderer extends MapRenderer {

    private final PlayerPositionHistory positionHistory;
    private final LeaderManager leaderManager;
    private final MapHunter plugin;

    public HunterMapRenderer(MapHunter plugin, PlayerPositionHistory positionHistory, LeaderManager leaderManager) {
        super(true);
        this.plugin = plugin;
        this.positionHistory = positionHistory;
        this.leaderManager = leaderManager;
    }

    @Override
    @SuppressWarnings("deprecation")
    public void render(MapView map, MapCanvas canvas, Player player) {
        while (canvas.getCursors().size() > 0) {
            canvas.getCursors().removeCursor(canvas.getCursors().getCursor(0));
        }

        int centerX = map.getCenterX();
        int centerZ = map.getCenterZ();
        
        int scaleMultiplier = 1;
        switch (map.getScale()) {   
            case CLOSEST: scaleMultiplier = 1; break;
            case CLOSE: scaleMultiplier = 2; break;
            case NORMAL: scaleMultiplier = 4; break;
            case FAR: scaleMultiplier = 8; break;
            case FARTHEST: scaleMultiplier = 16; break;
        }

        String showMode = plugin.getConfig().getString("map.show-players", "leaders-only");

        for (Player p : Bukkit.getOnlinePlayers()) {
            // Filter berdasarkan config
            if (!p.equals(player)) {
                if (showMode.equalsIgnoreCase("leaders-only")) {
                    // Hanya tampilkan leader, backup, atau yang pegang map
                    if (!leaderManager.isLeader(p) && !hasMap(p)) {
                        continue;
                    }
                }
                // Jika "all", tampilkan semua player
            }

            Location loc;
            if (p.equals(player)) {
                loc = p.getLocation();
            } else {
                loc = positionHistory.getOldestPosition(p.getUniqueId());
                if (loc == null) {
                    loc = p.getLocation();
                }
            }

            if (!loc.getWorld().equals(map.getWorld())) {
                continue;
            }

            double diffX = loc.getX() - centerX;
            double diffZ = loc.getZ() - centerZ;

            int pixelX = (int) (64 + (diffX / scaleMultiplier));
            int pixelZ = (int) (64 + (diffZ / scaleMultiplier));

            if (pixelX <= 0) pixelX = 1;
            if (pixelX >= 127) pixelX = 126;
            if (pixelZ <= 0) pixelZ = 1;
            if (pixelZ >= 127) pixelZ = 126;

            byte cursorX = (byte) (pixelX * 2 - 128);
            byte cursorY = (byte) (pixelZ * 2 - 128);

            float yaw = loc.getYaw();
            byte direction = (byte) (Math.round(yaw / 22.5) & 0xF);

            MapCursor.Type cursorType = MapCursor.Type.WHITE_POINTER;
            String caption = p.getName();
            
            LeaderData leaderData = leaderManager.getLeaderData(p);
            if (leaderData != null) {
                cursorType = getCursorType(leaderData.getClanColor());
                org.bukkit.ChatColor cColor = ColorUtil.getChatColor(leaderData.getClanColor());
                caption = cColor + "[" + leaderData.getClanName() + "] " + p.getName();
            } else if (hasMap(p)) {
                cursorType = MapCursor.Type.WHITE_POINTER;
                caption = "§e[BACKUP] " + p.getName();
            } else {
                // Player biasa (mode: all)
                cursorType = MapCursor.Type.WHITE_POINTER;
                caption = "§7" + p.getName();
            }

            @SuppressWarnings("deprecation")
            MapCursor cursor = new MapCursor(cursorX, cursorY, direction, cursorType, true, caption);
            canvas.getCursors().addCursor(cursor);
        }
    }

    private boolean hasMap(Player player) {
        for (ItemStack item : player.getInventory().getContents()) {
            if (ItemUtil.isHunterMap(item) || ItemUtil.isBackupMap(item)) {
                return true;
            }
        }
        return false;
    }

    private MapCursor.Type getCursorType(String color) {
        if (color == null) return MapCursor.Type.WHITE_POINTER;
        switch (color.toUpperCase()) {
            case "RED": return MapCursor.Type.RED_POINTER;
            case "BLUE": return MapCursor.Type.BLUE_POINTER;
            case "GREEN": return MapCursor.Type.GREEN_POINTER;
            case "AQUA": return MapCursor.Type.BLUE_POINTER;
            case "YELLOW": return MapCursor.Type.WHITE_POINTER;
            case "PURPLE": return MapCursor.Type.WHITE_POINTER;
            case "ORANGE": return MapCursor.Type.RED_POINTER;
            default: return MapCursor.Type.WHITE_POINTER;
        }
    }
}
