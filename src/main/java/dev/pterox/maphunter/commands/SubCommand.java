package dev.pterox.maphunter.commands;

import dev.pterox.maphunter.MapHunter;
import org.bukkit.command.CommandSender;

import java.util.List;

public abstract class SubCommand {

    protected MapHunter plugin;

    public void setPlugin(MapHunter plugin) {
        this.plugin = plugin;
    }

    public abstract String getName();
    public abstract String getDescription();
    public abstract String getSyntax();
    public abstract void perform(CommandSender sender, String[] args);
    public abstract List<String> getTabCompletions(CommandSender sender, String[] args);
}
