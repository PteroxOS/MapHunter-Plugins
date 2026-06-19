package dev.pterox.maphunter.leader;

import java.util.UUID;

public class LeaderData {

    private final UUID uuid;
    private final String playerName;
    private final String clanName;
    private final String clanColor;
    private int mapId;
    private final long createdAt;
    private UUID backupUuid;

    public LeaderData(UUID uuid, String playerName, String clanName, String clanColor, int mapId, long createdAt) {
        this.uuid = uuid;
        this.playerName = playerName;
        this.clanName = clanName;
        this.clanColor = clanColor;
        this.mapId = mapId;
        this.createdAt = createdAt;
        this.backupUuid = null;
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getPlayerName() {
        return playerName;
    }

    public String getClanName() {
        return clanName;
    }

    public String getClanColor() {
        return clanColor;
    }

    public int getMapId() {
        return mapId;
    }

    public void setMapId(int mapId) {
        this.mapId = mapId;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public UUID getBackupUuid() {
        return backupUuid;
    }

    public void setBackupUuid(UUID backupUuid) {
        this.backupUuid = backupUuid;
    }
}
