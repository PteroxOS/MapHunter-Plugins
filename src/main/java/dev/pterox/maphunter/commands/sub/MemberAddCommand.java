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
import java.util.UUID;

public class MemberAddCommand extends SubCommand {

    private final TeamMemberManager teamMemberManager;

    public MemberAddCommand(TeamMemberManager teamMemberManager) {
        this.teamMemberManager = teamMemberManager;
    }

    @Override
    public String getName() {
        return "memberadd";
    }

    @Override
    public String getDescription() {
        return "Tambah player sebagai member team";
    }

    @Override
    public String getSyntax() {
        return "/rmh memberadd <player> <teamName>";
    }

    @Override
    public void perform(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage(MessageUtil.color("&8[&b&lMapHunter&8] &r&cSyntax: " + getSyntax()));
            return;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage(MessageUtil.color("&8[&b&lMapHunter&8] &r&cPlayer tidak ditemukan."));
            return;
        }

        String teamName = args[2];

        // Tambah ke team
        teamMemberManager.addPlayerToTeam(target.getUniqueId(), teamName);

        sender.sendMessage(MessageUtil.color(""));
        sender.sendMessage(MessageUtil.color("&a&m                              "));
        sender.sendMessage(MessageUtil.color("&8[&b&lMapHunter&8] &r&a&l✓ MEMBER DITAMBAHKAN"));
        sender.sendMessage(MessageUtil.color("&8[&b&lMapHunter&8] &r&f" + target.getName() + " &fditambahkan ke team &e" + teamName));
        sender.sendMessage(MessageUtil.color("&a&m                              "));
        sender.sendMessage(MessageUtil.color(""));

        LogUtil.log("[Member] Added: " + target.getName() + " → " + teamName);
    }

    @Override
    public List<String> getTabCompletions(CommandSender sender, String[] args) {
        if (args.length == 2) {
            List<String> names = new ArrayList<>();
            for (Player p : Bukkit.getOnlinePlayers()) {
                names.add(p.getName());
            }
            return names;
        } else if (args.length == 3) {
            return new ArrayList<>(teamMemberManager.getAllTeamNames());
        }
        return new ArrayList<>();
    }
}
