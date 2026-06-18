package dev.pterox.maphunter.commands.sub;

import dev.pterox.maphunter.commands.SubCommand;
import dev.pterox.maphunter.event.EventManager;
import dev.pterox.maphunter.util.MessageUtil;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;

public class EventStopCommand extends SubCommand {

    private final EventManager eventManager;

    public EventStopCommand(EventManager eventManager) {
        this.eventManager = eventManager;
    }

    @Override
    public String getName() {
        return "stop";
    }

    @Override
    public String getDescription() {
        return "Stop the MapHunter event";
    }

    @Override
    public String getSyntax() {
        return "/rmh event stop";
    }

    @Override
    public void perform(CommandSender sender, String[] args) {
        if (!eventManager.isEventActive()) {
            MessageUtil.sendConfigMessage(sender, "messages.event-not-active", "&cEvent sedang tidak berjalan.", plugin);
            return;
        }

        eventManager.stopEvent();
        sender.sendMessage(MessageUtil.color("&aEvent has been stopped successfully."));
    }

    @Override
    public List<String> getTabCompletions(CommandSender sender, String[] args) {
        return new ArrayList<>();
    }
}
