package dev.pterox.maphunter.commands.sub;

import dev.pterox.maphunter.commands.SubCommand;
import dev.pterox.maphunter.leader.LeaderManager;
import dev.pterox.maphunter.util.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LeaderAddCommand extends SubCommand {

    private final LeaderManager leaderManager;

    public LeaderAddCommand(LeaderManager leaderManager) {
        this.leaderManager = leaderManager;
    }

    @Override
    public String getName() {
        return "add";
    }

    @Override
    public String getDescription() {
        return "Mendaftarkan player sebagai leader clan";
    }

    @Override
    public String getSyntax() {
        return "/rmh leader add <player> <clanName> <color>";
    }

    @Override
    public void perform(CommandSender sender, String[] args) {
        if (args.length < 5) {
            sender.sendMessage(MessageUtil.color("&cSyntax: " + getSyntax()));
            return;
        }

        Player target = Bukkit.getPlayer(args[2]);
        if (target == null) {
            MessageUtil.sendConfigMessage(sender, "messages.player-not-found", "&cPlayer tidak ditemukan.", plugin);
            return;
        }

        if (leaderManager.isLeader(target)) {
            MessageUtil.sendConfigMessage(sender, "messages.already-leader", "&cPlayer tersebut sudah terdaftar sebagai leader.", plugin);
            return;
        }

        String clanName = args[3];
        String color = args[4].toUpperCase();

        List<String> validColors = Arrays.asList("RED", "BLUE", "GREEN", "YELLOW", "PURPLE", "ORANGE", "AQUA", "PINK", "WHITE");
        if (!validColors.contains(color)) {
            sender.sendMessage(MessageUtil.color("&cWarna tidak valid. Pilihan: " + String.join(", ", validColors)));
            return;
        }

        leaderManager.addLeader(target, clanName, color);
        sender.sendMessage(MessageUtil.color(""));
        sender.sendMessage(MessageUtil.color("&a&m                              "));
        sender.sendMessage(MessageUtil.color("&8[&b&lMapHunter&8] &r&a&l✓ LEADER DITAMBAHKAN"));
        sender.sendMessage(MessageUtil.color("&8[&b&lMapHunter&8] &r&f" + target.getName() + " &fadalah ketua clan &e" + clanName + " &f(" + color + ")"));
        sender.sendMessage(MessageUtil.color("&a&m                              "));
        sender.sendMessage(MessageUtil.color(""));
    }

    @Override
    public List<String> getTabCompletions(CommandSender sender, String[] args) {
        if (args.length == 3) {
            List<String> names = new ArrayList<>();
            for (Player p : Bukkit.getOnlinePlayers()) {
                names.add(p.getName());
            }
            return names;
        } else if (args.length == 4) {
            return Arrays.asList("<nama_clan>");
        } else if (args.length == 5) {
            return Arrays.asList("RED", "BLUE", "GREEN", "YELLOW", "PURPLE", "ORANGE", "AQUA", "WHITE");
        }
        return new ArrayList<>();
    }
}
