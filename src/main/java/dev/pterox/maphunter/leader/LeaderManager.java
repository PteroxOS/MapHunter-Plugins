package dev.pterox.maphunter.leader;

import dev.pterox.maphunter.storage.LeaderRepository;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class LeaderManager {

    private final LeaderRepository repository;
    private final Map<UUID, LeaderData> cachedLeaders = new HashMap<>();

    public LeaderManager(LeaderRepository repository) {
        this.repository = repository;
    }

    public void loadAll() {
        cachedLeaders.clear();
        for (LeaderData data : repository.findAll()) {
            cachedLeaders.put(data.getUuid(), data);
        }
    }

    public boolean isLeader(Player player) {
        return cachedLeaders.containsKey(player.getUniqueId());
    }

    public LeaderData getLeaderData(Player player) {
        return cachedLeaders.get(player.getUniqueId());
    }

    public LeaderData getLeaderData(UUID uuid) {
        LeaderData data = cachedLeaders.get(uuid);
        if (data == null) {
            // Load dari database untuk keperluan read-only (replacedByBackup, backupUuid, dll)
            data = repository.findByUuid(uuid);
            // JANGAN tambah ke cache! Agar tidak dihitung sebagai leader aktif
        }
        return data;
    }

    public void addLeader(Player player, String clanName, String color) {
        LeaderData data = new LeaderData(
                player.getUniqueId(),
                player.getName(),
                clanName,
                color,
                -1,
                System.currentTimeMillis()
        );
        cachedLeaders.put(player.getUniqueId(), data);
        repository.save(data);
    }

    public void removeLeader(Player player) {
        cachedLeaders.remove(player.getUniqueId());
        repository.delete(player.getUniqueId());
    }
    
    public void removeLeader(UUID uuid) {
        cachedLeaders.remove(uuid);
        repository.delete(uuid);
    }

    public void removeFromCacheOnly(UUID uuid) {
        cachedLeaders.remove(uuid);
    }

    public void saveLeader(LeaderData data) {
        cachedLeaders.put(data.getUuid(), data);
        repository.save(data);
    }

    public void saveToDbOnly(LeaderData data) {
        repository.save(data);
    }

    public List<LeaderData> getAllLeaders() {
        return new ArrayList<>(cachedLeaders.values());
    }
}
