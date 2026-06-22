package dev.pterox.maphunter.commands.sub;

import dev.pterox.maphunter.MapHunter;
import dev.pterox.maphunter.commands.SubCommand;
import dev.pterox.maphunter.integration.TeamMemberManager;
import dev.pterox.maphunter.util.LogUtil;
import dev.pterox.maphunter.util.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class MemberRemoveCommand extends SubCommand {

    private final TeamMemberManager teamMemberManager;

    public MemberRemoveCommand(TeamMemberManager teamMemberManager) {
        this.teamMemberManager = teamMemberManager;
    }

    @Override
    public String getName() {
        return "memberremove";
    }

    @Override
    public String getDescription() {
        return "Hapus player dari team";
    }

    @Override
    public String getSyntax() {
        return "/rmh memberremove <player>";
    }

    @Override
    public void perform(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(MessageUtil.color("&8[&b&lMapHunter&8] &r&cSyntax: " + getSyntax()));
            return;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage(MessageUtil.color("&8[&b&lMapHunter&8] &r&cPlayer tidak ditemukan."));
            return;
        }

        String teamName = teamMemberManager.getPlayerTeam(target);
        if (teamName == null) {
            sender.sendMessage(MessageUtil.color("&8[&b&lMapHunter&8] &r&cPlayer tersebut bukan member team manapun."));
            return;
        }

        teamMemberManager.removePlayerFromTeam(target.getUniqueId());

        sender.sendMessage(MessageUtil.color(""));
        sender.sendMessage(MessageUtil.color("&c&m                              "));
        sender.sendMessage(MessageUtil.color("&8[&b&lMapHunter&8] &r&c&l✗ MEMBER DIHAPUS"));
        sender.sendMessage(MessageUtil.color("&8[&b&lMapHunter&8] &r&f" + target.getName() + " &fdihapus dari team &e" + teamName));
        sender.sendMessage(MessageUtil.color("&c&m                              "));
        sender.sendMessage(MessageUtil.color(""));

        LogUtil.log("[Member] Removed: " + target.getName() + " from " + teamName);
    }

    @Override
    public List<String> getTabCompletions(CommandSender sender, String[] args) {
        if (args.length == 2) {
            List<String> names = new ArrayList<>();
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (teamMemberManager.isPlayerInTeam(p)) {
                    names.add(p.getName());
                }
            }
            return names;
        }
        return new ArrayList<>();
    }
}
