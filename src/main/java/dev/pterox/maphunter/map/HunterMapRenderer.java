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
        super(false); // Not contextual, map looks the same for everyone holding it
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

        // Draw basic background (optional, or just leave it blank transparent)
        // A simple parchment-like background
        for (int x = 0; x < 128; x++) {
            for (int z = 0; z < 128; z++) {
                canvas.setPixel(x, z, MapPalette.PALE_BLUE);
            }
        }

        // Add crosshair for center
        canvas.setPixel(64, 64, MapPalette.RED);

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
            Location oldLoc = positionHistory.getOldestPosition(p.getUniqueId());
            if (oldLoc == null) {
                // If no history yet, use current
                oldLoc = p.getLocation();
            }

            if (!oldLoc.getWorld().equals(map.getWorld())) {
                continue;
            }

            // Calculate map pixel coordinate
            // Map covers 128x128 pixels. Center is 64, 64.
            double diffX = oldLoc.getX() - centerX;
            double diffZ = oldLoc.getZ() - centerZ;

            int pixelX = (int) (64 + (diffX / scaleMultiplier));
            int pixelZ = (int) (64 + (diffZ / scaleMultiplier));

            if (pixelX >= 0 && pixelX < 128 && pixelZ >= 0 && pixelZ < 128) {
                byte color = MapPalette.GRAY_1; // Default
                LeaderData leaderData = leaderManager.getLeaderData(p);
                if (leaderData != null) {
                    color = ColorUtil.getMapColor(leaderData.getClanColor());
                } else {
                    // Non-leader clan members?
                    // The prompt says "colored by their clan (if leader/member of a clan)"
                    // But LeaderManager only tracks leaders. 
                    // To color members, we'd need a clan plugin integration.
                    // Assuming we just color the leaders with their clan color for now,
                    // or if the prompt implies we only have leaders registered, we color them.
                }

                // Make a 3x3 square for visibility
                for (int dx = -1; dx <= 1; dx++) {
                    for (int dz = -1; dz <= 1; dz++) {
                        int nx = pixelX + dx;
                        int nz = pixelZ + dz;
                        if (nx >= 0 && nx < 128 && nz >= 0 && nz < 128) {
                            canvas.setPixel(nx, nz, color);
                        }
                    }
                }
            }
        }
    }
}
