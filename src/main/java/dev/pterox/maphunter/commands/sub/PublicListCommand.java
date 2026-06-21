package dev.pterox.maphunter.commands.sub;

import dev.pterox.maphunter.commands.SubCommand;
import dev.pterox.maphunter.leader.LeaderData;
import dev.pterox.maphunter.leader.LeaderManager;
import dev.pterox.maphunter.util.ColorUtil;
import dev.pterox.maphunter.util.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class PublicListCommand extends SubCommand {

    private final LeaderManager leaderManager;

    public PublicListCommand(LeaderManager leaderManager) {
        this.leaderManager = leaderManager;
    }

    @Override
    public String getName() {
        return "list";
    }

    @Override
    public String getDescription() {
        return "Melihat daftar team yang mengikuti MapHunter";
    }

    @Override
    public String getSyntax() {
        return "/rmh list";
    }

    @Override
    public void perform(CommandSender sender, String[] args) {
        List<LeaderData> leaders = leaderManager.getAllLeaders();
        if (leaders.isEmpty()) {
            sender.sendMessage(MessageUtil.color("&cBelum ada team yang mengikuti MapHunter."));
            return;
        }

        sender.sendMessage(MessageUtil.color("&8&m                                                      "));
        sender.sendMessage(MessageUtil.color("&b&l MAP HUNTER &7- &eDaftar Team"));
        sender.sendMessage(MessageUtil.color("&8&m                                                      "));

        int index = 1;
        for (LeaderData data : leaders) {
            Player player = Bukkit.getPlayer(data.getUuid());
            boolean isOnline = player != null && player.isOnline();
            String statusIcon = isOnline ? "&a●" : "&c●";
            
            org.bukkit.ChatColor clanColor = ColorUtil.getChatColor(data.getClanColor());
            
            sender.sendMessage(MessageUtil.color("&f" + index + ". " + statusIcon + " " + clanColor + data.getClanName()));
            index++;
        }
        
        sender.sendMessage(MessageUtil.color("&8&m                                                      "));
        sender.sendMessage(MessageUtil.color("&7Total: &e" + leaders.size() + " &7team aktif"));
    }

    @Override
    public List<String> getTabCompletions(CommandSender sender, String[] args) {
        return new ArrayList<>();
    }
}
