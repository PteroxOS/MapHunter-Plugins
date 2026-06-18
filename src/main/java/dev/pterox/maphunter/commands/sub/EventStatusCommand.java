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
        return "Check MapHunter event status";
    }

    @Override
    public String getSyntax() {
        return "/rmh event status";
    }

    @Override
    public void perform(CommandSender sender, String[] args) {
        if (eventManager.isEventActive()) {
            sender.sendMessage(MessageUtil.color("&aThe MapHunter event is currently ACTIVE."));
        } else {
            sender.sendMessage(MessageUtil.color("&cThe MapHunter event is currently INACTIVE."));
        }
    }

    @Override
    public List<String> getTabCompletions(CommandSender sender, String[] args) {
        return new ArrayList<>();
    }
}
