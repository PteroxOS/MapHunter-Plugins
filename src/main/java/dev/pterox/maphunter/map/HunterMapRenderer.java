package dev.pterox.maphunter.map;

import dev.pterox.maphunter.leader.LeaderData;
import dev.pterox.maphunter.leader.LeaderManager;
import dev.pterox.maphunter.util.ColorUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapCursor;
import org.bukkit.map.MapPalette;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;

public class HunterMapRenderer extends MapRenderer {

    private final PlayerPositionHistory positionHistory;
    private final LeaderManager leaderManager;

    public HunterMapRenderer(PlayerPositionHistory positionHistory, LeaderManager leaderManager) {
        super(true); // Contextual, so it renders per-player properly
        this.positionHistory = positionHistory;
        this.leaderManager = leaderManager;
    }

    @Override
    @SuppressWarnings("deprecation")
    public void render(MapView map, MapCanvas canvas, Player player) {
        // Clear old cursors
        while (canvas.getCursors().size() > 0) {
            canvas.getCursors().removeCursor(canvas.getCursors().getCursor(0));
        }

        // Center location based on map coordinates
        int centerX = map.getCenterX();
        int centerZ = map.getCenterZ();
        
        // Scale logic
        int scaleMultiplier = 1;
        switch (map.getScale()) {   
            case CLOSEST: scaleMultiplier = 1; break;
            case CLOSE: scaleMultiplier = 2; break;
            case NORMAL: scaleMultiplier = 4; break;
            case FAR: scaleMultiplier = 8; break;
            case FARTHEST: scaleMultiplier = 16; break;
        }

        for (Player p : Bukkit.getOnlinePlayers()) {
            Location loc;
            if (p.equals(player)) {
                loc = p.getLocation(); // Pemain melihat dirinya sendiri secara real-time
            } else {
                loc = positionHistory.getOldestPosition(p.getUniqueId());
                if (loc == null) {
                    loc = p.getLocation();
                }
            }

            if (!loc.getWorld().equals(map.getWorld())) {
                continue;
            }

            // Calculate map pixel coordinate
            // Map covers 128x128 pixels. Center is 64, 64.
            double diffX = loc.getX() - centerX;
            double diffZ = loc.getZ() - centerZ;

            int pixelX = (int) (64 + (diffX / scaleMultiplier));
            int pixelZ = (int) (64 + (diffZ / scaleMultiplier));

            // Clamp pixel coordinates so the cursor stays on the edge if out of bounds
            boolean outOfBounds = false;
            if (pixelX <= 0) { pixelX = 1; outOfBounds = true; }
            if (pixelX >= 127) { pixelX = 126; outOfBounds = true; }
            if (pixelZ <= 0) { pixelZ = 1; outOfBounds = true; }
            if (pixelZ >= 127) { pixelZ = 126; outOfBounds = true; }

            // Convert 0-127 pixel coordinates to -128 to 127 byte coordinates for MapCursor
            byte cursorX = (byte) (pixelX * 2 - 128);
            byte cursorY = (byte) (pixelZ * 2 - 128);

            // Calculate direction (yaw to MapCursor 0-15)
            float yaw = loc.getYaw() + 180.0F;
            // Bukkit yaw: 0 is South, 90 is West, 180 is North, 270 is East
            // MapCursor direction: 0 is South, 4 is West, 8 is North, 12 is East
            byte direction = (byte) (Math.round(yaw / 22.5) & 0xF);

            MapCursor.Type cursorType = MapCursor.Type.WHITE_POINTER;
            LeaderData leaderData = leaderManager.getLeaderData(p);
            if (leaderData != null) {
                cursorType = getCursorType(leaderData.getClanColor());
            }
            
            // Optional: You could use a smaller pointer when out of bounds, but using the same is fine.

            // Add cursor for the tracked player
            @SuppressWarnings("deprecation")
            MapCursor cursor = new MapCursor(cursorX, cursorY, direction, cursorType, true, p.getName());
            canvas.getCursors().addCursor(cursor);
        }
    }

    private MapCursor.Type getCursorType(String color) {
        if (color == null) return MapCursor.Type.WHITE_POINTER;
        switch (color.toUpperCase()) {
            case "RED": return MapCursor.Type.RED_POINTER;
            case "BLUE": return MapCursor.Type.BLUE_POINTER;
            case "GREEN": return MapCursor.Type.GREEN_POINTER;
            case "AQUA": return MapCursor.Type.BLUE_POINTER; // AQUA mapping
            case "YELLOW": return MapCursor.Type.WHITE_POINTER; // No explicit yellow in 1.20, fallback
            case "PURPLE": return MapCursor.Type.WHITE_POINTER; // Fallback
            case "ORANGE": return MapCursor.Type.RED_POINTER; // Fallback
            default: return MapCursor.Type.WHITE_POINTER;
        }
    }
}
