package dev.pterox.maphunter.map;

import org.bukkit.Location;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.UUID;

public class PlayerPositionHistory {

    private final Map<UUID, LinkedList<Location>> history = new HashMap<>();
    private final int maxCapacity;

    public PlayerPositionHistory(int maxCapacity) {
        this.maxCapacity = maxCapacity;
    }

    public void recordPosition(UUID uuid, Location location) {
        history.putIfAbsent(uuid, new LinkedList<>());
        LinkedList<Location> list = history.get(uuid);
        list.addLast(location.clone());
        if (list.size() > maxCapacity) {
            list.removeFirst();
        }
    }

    public Location getOldestPosition(UUID uuid) {
        LinkedList<Location> list = history.get(uuid);
        if (list != null && !list.isEmpty()) {
            return list.getFirst();
        }
        return null;
    }
    
    public void clearHistory(UUID uuid) {
        history.remove(uuid);
    }
    
    public void clearAll() {
        history.clear();
    }
}
