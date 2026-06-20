package dev.pterox.maphunter.commands.sub;

import dev.pterox.maphunter.commands.SubCommand;
import dev.pterox.maphunter.event.EventManager;
import dev.pterox.maphunter.util.MessageUtil;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;

public class EventStartCommand extends SubCommand {

    private final EventManager eventManager;

    public EventStartCommand(EventManager eventManager) {
        this.eventManager = eventManager;
    }

    @Override
    public String getName() {
        return "start";
    }

    @Override
    public String getDescription() {
        return "Memulai event MapHunter";
    }

    @Override
    public String getSyntax() {
        return "/rmh event start";
    }

    @Override
    public void perform(CommandSender sender, String[] args) {
        if (eventManager.isEventActive()) {
            MessageUtil.sendConfigMessage(sender, "messages.event-already-active", "&cEvent sudah berjalan.", plugin);
            return;
        }

        // Cek inventory leaders apakah ada slot kosong
        dev.pterox.maphunter.leader.LeaderManager leaderManager = plugin.getLeaderManager();
        if (leaderManager.getAllLeaders().isEmpty()) {
            sender.sendMessage(MessageUtil.color("&cEvent gagal dimulai karena belum ada leader yang di-set! Gunakan &e/rmh leader add &cuntuk menambahkan."));
            return;
        }

        for (dev.pterox.maphunter.leader.LeaderData data : leaderManager.getAllLeaders()) {
            org.bukkit.entity.Player p = org.bukkit.Bukkit.getPlayer(data.getUuid());
            if (p != null && p.isOnline()) {
                if (p.getInventory().firstEmpty() == -1) {
                    sender.sendMessage(MessageUtil.color("&cEvent gagal dimulai karena inventory &e" + p.getName() + " &cpenuh! Harap kosongkan minimal 1 slot."));
                    return;
                }
            }
        }

        eventManager.startEvent();
        sender.sendMessage(MessageUtil.color("&aEvent berhasil dimulai."));
    }

    @Override
    public List<String> getTabCompletions(CommandSender sender, String[] args) {
        return new ArrayList<>();
    }
}
