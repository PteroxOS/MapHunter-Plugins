package dev.pterox.maphunter.commands.sub;

import dev.pterox.maphunter.commands.SubCommand;
import dev.pterox.maphunter.event.EventManager;
import dev.pterox.maphunter.util.MessageUtil;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;

public class EventStartCommand extends SubCommand {

    private final EventManager eventManager;

    public EventStartCommand(EventManager eventManager) {
        this.eventManager = eventManager;
    }

    @Override
    public String getName() {
        return "start";
    }

    @Override
    public String getDescription() {
        return "Memulai event MapHunter";
    }

    @Override
    public String getSyntax() {
        return "/rmh event start";
    }

    @Override
    public void perform(CommandSender sender, String[] args) {
        if (eventManager.isEventActive()) {
            MessageUtil.sendConfigMessage(sender, "messages.event-already-active", "&cEvent sudah berjalan.", plugin);
            return;
        }

        eventManager.startEvent();
        sender.sendMessage(MessageUtil.color("&aEvent berhasil dimulai."));
    }

    @Override
    public List<String> getTabCompletions(CommandSender sender, String[] args) {
        return new ArrayList<>();
    }
}
