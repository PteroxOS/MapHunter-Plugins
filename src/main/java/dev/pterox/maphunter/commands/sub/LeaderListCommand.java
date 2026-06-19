package dev.pterox.maphunter.commands.sub;

import dev.pterox.maphunter.commands.SubCommand;
import dev.pterox.maphunter.leader.LeaderData;
import dev.pterox.maphunter.leader.LeaderManager;
import dev.pterox.maphunter.util.MessageUtil;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;

public class LeaderListCommand extends SubCommand {

    private final LeaderManager leaderManager;

    public LeaderListCommand(LeaderManager leaderManager) {
        this.leaderManager = leaderManager;
    }

    @Override
    public String getName() {
        return "list";
    }

    @Override
    public String getDescription() {
        return "Melihat daftar semua leader clan";
    }

    @Override
    public String getSyntax() {
        return "/rmh leader list";
    }

    @Override
    public void perform(CommandSender sender, String[] args) {
        List<LeaderData> leaders = leaderManager.getAllLeaders();
        if (leaders.isEmpty()) {
            sender.sendMessage(MessageUtil.color("§cBelum ada leader yang terdaftar."));
            return;
        }

        sender.sendMessage(MessageUtil.color("§e--- Daftar Leader ---"));
        for (LeaderData data : leaders) {
            sender.sendMessage(MessageUtil.color("&b" + data.getPlayerName() + " &f- Clan: &e" + data.getClanName() + " &f- Color: " + data.getClanColor()));
        }
    }

    @Override
    public List<String> getTabCompletions(CommandSender sender, String[] args) {
        return new ArrayList<>();
    }
}
