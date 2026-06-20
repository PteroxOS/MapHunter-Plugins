package dev.pterox.maphunter.commands.sub;

import dev.pterox.maphunter.commands.SubCommand;
import dev.pterox.maphunter.util.MessageUtil;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;

public class ReloadCommand extends SubCommand {

    @Override
    public String getName() {
        return "reload";
    }

    @Override
    public String getDescription() {
        return "Reload file config.yml dan messages.yml";
    }

    @Override
    public String getSyntax() {
        return "/rmh reload";
    }

    @Override
    public void perform(CommandSender sender, String[] args) {
        plugin.reloadConfig();
        plugin.getMessageConfig().reloadConfig();
        sender.sendMessage(MessageUtil.color("&a[MapHunter] Konfigurasi berhasil di-reload!"));
    }

    @Override
    public List<String> getTabCompletions(CommandSender sender, String[] args) {
        return new ArrayList<>();
    }
}
