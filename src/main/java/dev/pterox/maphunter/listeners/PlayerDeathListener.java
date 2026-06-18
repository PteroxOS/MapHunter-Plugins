package dev.pterox.maphunter.listeners;

import dev.pterox.maphunter.leader.LeaderData;
import dev.pterox.maphunter.leader.LeaderManager;
import dev.pterox.maphunter.notification.NotificationManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

public class PlayerDeathListener implements Listener {

    private final LeaderManager leaderManager;
    private final NotificationManager notificationManager;

    public PlayerDeathListener(LeaderManager leaderManager, NotificationManager notificationManager) {
        this.leaderManager = leaderManager;
        this.notificationManager = notificationManager;
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        if (leaderManager.isLeader(player)) {
            LeaderData data = leaderManager.getLeaderData(player);
            if (data != null) {
                notificationManager.broadcastLeaderDeath(data.getClanName(), player.getName());
            }
        }
    }
}
