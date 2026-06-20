package dev.pterox.maphunter.commands.sub;

import dev.pterox.maphunter.commands.SubCommand;
import dev.pterox.maphunter.event.EventManager;
import dev.pterox.maphunter.util.MessageUtil;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;

public class EventStopCommand extends SubCommand {

    private final EventManager eventManager;

    public EventStopCommand(EventManager eventManager) {
        this.eventManager = eventManager;
    }

    @Override
    public String getName() {
        return "stop";
    }

    @Override
    public String getDescription() {
        return "Menghentikan event MapHunter";
    }

    @Override
    public String getSyntax() {
        return "/rmh event stop";
    }

    @Override
    public void perform(CommandSender sender, String[] args) {
        if (!eventManager.isEventActive()) {
            MessageUtil.sendConfigMessage(sender, "messages.event-not-active", "&cEvent sedang tidak berjalan.", plugin);
            return;
        }

        dev.pterox.maphunter.leader.LeaderManager leaderManager = plugin.getLeaderManager();
        for (dev.pterox.maphunter.leader.LeaderData data : leaderManager.getAllLeaders()) {
            org.bukkit.entity.Player p = org.bukkit.Bukkit.getPlayer(data.getUuid());
            if (p != null) {
                plugin.getMapManager().removeHunterMap(p);
            }
            leaderManager.removeLeader(data.getUuid());
        }

        eventManager.stopEvent();
        sender.sendMessage(MessageUtil.color("&aEvent berhasil dihentikan dan semua leader telah dihapus."));
    }

    @Override
    public List<String> getTabCompletions(CommandSender sender, String[] args) {
        return new ArrayList<>();
    }
}
