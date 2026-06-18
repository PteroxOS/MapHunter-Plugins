package dev.pterox.maphunter.commands.sub;

import dev.pterox.maphunter.commands.SubCommand;
import dev.pterox.maphunter.event.EventManager;
import dev.pterox.maphunter.util.MessageUtil;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;

public class EventStatusCommand extends SubCommand {

    private final EventManager eventManager;

    public EventStatusCommand(EventManager eventManager) {
        this.eventManager = eventManager;
    }

    @Override
    public String getName() {
        return "status";
    }

    @Override
    public String getDescription() {
        return "Melihat status event MapHunter";
    }

    @Override
    public String getSyntax() {
        return "/rmh event status";
    }

    @Override
    public void perform(CommandSender sender, String[] args) {
        if (eventManager.isEventActive()) {
            sender.sendMessage(MessageUtil.color("&aEvent MapHunter saat ini SEDANG AKTIF."));
        } else {
            sender.sendMessage(MessageUtil.color("&cEvent MapHunter saat ini TIDAK AKTIF."));
        }
    }

    @Override
    public List<String> getTabCompletions(CommandSender sender, String[] args) {
        return new ArrayList<>();
    }
}
