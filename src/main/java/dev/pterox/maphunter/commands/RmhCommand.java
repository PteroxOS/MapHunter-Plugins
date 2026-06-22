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

    public void registerPublicSubCommand(SubCommand subCommand) {
        subCommand.setPlugin(plugin);
        subCommands.put("publiclist", subCommand);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            if (!sender.hasPermission("maphunter.admin")) {
                sendPublicHelp(sender);
            } else {
                sendHelp(sender);
            }
            return true;
        }

        String subCommandType = args[0].toLowerCase();
        
        // /rmh list - bisa diakses semua player tanpa permission
        if (subCommandType.equals("list")) {
            SubCommand sub = subCommands.get("publiclist");
            if (sub != null) {
                sub.perform(sender, args);
                return true;
            }
        }
        
        // Admin only commands
        if (!sender.hasPermission("maphunter.admin")) {
            MessageUtil.sendConfigMessage(sender, "messages.no-permission", "&cKamu tidak punya izin!", plugin);
            return true;
        }
        
        // Handle nested subcommands manually here for simplicity,
        // or route based on arg[0]
        if (subCommandType.equals("leader") && args.length > 1) {
            SubCommand sub = subCommands.get(args[1].toLowerCase());
            if (sub != null && (sub.getName().equals("add") || sub.getName().equals("remove") || sub.getName().equals("list") || sub.getName().equals("backup") || sub.getName().equals("restore"))) {
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
        } else         if (subCommandType.equals("reload")) {
            SubCommand sub = subCommands.get("reload");
            if (sub != null) {
                sub.perform(sender, args);
                return true;
            }
        } else if (subCommandType.equals("sync")) {
            SubCommand sub = subCommands.get("sync");
            if (sub != null) {
                sub.perform(sender, args);
                return true;
            }
        } else if (subCommandType.equals("memberadd")) {
            SubCommand sub = subCommands.get("memberadd");
            if (sub != null) {
                sub.perform(sender, args);
                return true;
            }
        } else if (subCommandType.equals("memberremove")) {
            SubCommand sub = subCommands.get("memberremove");
            if (sub != null) {
                sub.perform(sender, args);
                return true;
            }
        }

        sendHelp(sender);
        return true;
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(MessageUtil.color("&8&m                                                      "));
        sender.sendMessage(MessageUtil.color("&b&l MAP HUNTER &7- &eAdmin Commands"));
        sender.sendMessage(MessageUtil.color("&8&m                                                      "));
        sender.sendMessage(MessageUtil.color("&b/rmh list &7- &fLihat daftar team"));
        sender.sendMessage(MessageUtil.color("&b/rmh leader add &7- &fTambah leader"));
        sender.sendMessage(MessageUtil.color("&b/rmh leader remove &7- &fHapus leader"));
        sender.sendMessage(MessageUtil.color("&b/rmh leader list &7- &fDetail leader"));
        sender.sendMessage(MessageUtil.color("&b/rmh leader backup &7- &fSet backup leader"));
        sender.sendMessage(MessageUtil.color("&b/rmh leader restore &7- &fKembalikan map ke leader utama"));
        sender.sendMessage(MessageUtil.color("&b/rmh map give &7- &fBerikan map"));
        sender.sendMessage(MessageUtil.color("&b/rmh map remove &7- &fAmbil map"));
        sender.sendMessage(MessageUtil.color("&b/rmh event start &7- &fMulai event"));
        sender.sendMessage(MessageUtil.color("&b/rmh event stop &7- &fHentikan event"));
        sender.sendMessage(MessageUtil.color("&b/rmh event status &7- &fCek status"));
        sender.sendMessage(MessageUtil.color("&b/rmh reload &7- &fReload config"));
        sender.sendMessage(MessageUtil.color("&b/rmh sync &7- &fSync dari BetterTeams"));
        sender.sendMessage(MessageUtil.color("&b/rmh memberadd &7- &fTambah member team"));
        sender.sendMessage(MessageUtil.color("&b/rmh memberremove &7- &fHapus member team"));
        sender.sendMessage(MessageUtil.color("&8&m                                                      "));
    }

    private void sendPublicHelp(CommandSender sender) {
        sender.sendMessage(MessageUtil.color("&8&m                                                      "));
        sender.sendMessage(MessageUtil.color("&b&l MAP HUNTER &7- &eMapHunter Commands"));
        sender.sendMessage(MessageUtil.color("&8&m                                                      "));
        sender.sendMessage(MessageUtil.color("&b/rmh list &7- &fLihat daftar team"));
        sender.sendMessage(MessageUtil.color("&8&m                                                      "));
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> types = new ArrayList<>();
            types.add("list");
            if (sender.hasPermission("maphunter.admin")) {
                types.add("leader");
                types.add("map");
                types.add("event");
                types.add("reload");
                types.add("sync");
                types.add("memberadd");
                types.add("memberremove");
            }
            return filter(types, args[0]);
        }

        if (!sender.hasPermission("maphunter.admin")) {
            return new ArrayList<>();
        }

        if (args.length == 2) {
            List<String> actions = new ArrayList<>();
            if (args[0].equalsIgnoreCase("leader")) {
                actions.add("add");
                actions.add("remove");
                actions.add("list");
                actions.add("backup");
                actions.add("restore");
            } else if (args[0].equalsIgnoreCase("map")) {
                actions.add("give");
                actions.add("remove");
            } else if (args[0].equalsIgnoreCase("event")) {
                actions.add("start");
                actions.add("stop");
                actions.add("status");
            } else if (args[0].equalsIgnoreCase("sync")) {
                actions.add("confirm");
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
