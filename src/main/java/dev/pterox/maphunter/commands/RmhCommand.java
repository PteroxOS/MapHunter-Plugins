package dev.pterox.maphunter.commands;

import dev.pterox.maphunter.MapHunter;
import dev.pterox.maphunter.util.MessageUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RmhCommand implements CommandExecutor, TabCompleter {

    private final MapHunter plugin;
    private final Map<String, SubCommand> subCommands = new HashMap<>();

    public RmhCommand(MapHunter plugin) {
        this.plugin = plugin;
    }

    public void registerSubCommand(SubCommand subCommand) {
        subCommand.setPlugin(plugin);
        subCommands.put(subCommand.getName().toLowerCase(), subCommand);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("maphunter.admin")) {
            MessageUtil.sendConfigMessage(sender, "messages.no-permission", "&cKamu tidak punya izin!", plugin);
            return true;
        }

        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        String subCommandType = args[0].toLowerCase();
        
        // Handle nested subcommands manually here for simplicity,
        // or route based on arg[0]
        if (subCommandType.equals("leader") && args.length > 1) {
            SubCommand sub = subCommands.get(args[1].toLowerCase());
            if (sub != null && (sub.getName().equals("add") || sub.getName().equals("remove") || sub.getName().equals("list"))) {
                sub.perform(sender, args);
                return true;
            }
        } else if (subCommandType.equals("map") && args.length > 1) {
            SubCommand sub = subCommands.get(args[1].toLowerCase());
            if (sub != null && (sub.getName().equals("give") || sub.getName().equals("remove"))) {
                sub.perform(sender, args);
                return true;
            }
        } else if (subCommandType.equals("event") && args.length > 1) {
            SubCommand sub = subCommands.get(args[1].toLowerCase());
            if (sub != null && (sub.getName().equals("start") || sub.getName().equals("stop") || sub.getName().equals("status"))) {
                sub.perform(sender, args);
                return true;
            }
        }

        sendHelp(sender);
        return true;
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(MessageUtil.color("&e--- MapHunter Commands ---"));
        for (SubCommand sub : subCommands.values()) {
            sender.sendMessage(MessageUtil.color("&b" + sub.getSyntax() + " &f- " + sub.getDescription()));
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.hasPermission("maphunter.admin")) {
            return new ArrayList<>();
        }

        if (args.length == 1) {
            List<String> types = new ArrayList<>();
            types.add("leader");
            types.add("map");
            types.add("event");
            return filter(types, args[0]);
        }

        if (args.length == 2) {
            List<String> actions = new ArrayList<>();
            if (args[0].equalsIgnoreCase("leader")) {
                actions.add("add");
                actions.add("remove");
                actions.add("list");
            } else if (args[0].equalsIgnoreCase("map")) {
                actions.add("give");
                actions.add("remove");
            } else if (args[0].equalsIgnoreCase("event")) {
                actions.add("start");
                actions.add("stop");
                actions.add("status");
            }
            return filter(actions, args[1]);
        }

        if (args.length > 2) {
            SubCommand sub = subCommands.get(args[1].toLowerCase());
            if (sub != null) {
                return filter(sub.getTabCompletions(sender, args), args[args.length - 1]);
            }
        }

        return new ArrayList<>();
    }

    private List<String> filter(List<String> list, String query) {
        List<String> result = new ArrayList<>();
        for (String s : list) {
            if (s.toLowerCase().startsWith(query.toLowerCase())) {
                result.add(s);
            }
        }
        return result;
    }
}
