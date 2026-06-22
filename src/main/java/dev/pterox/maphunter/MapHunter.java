package dev.pterox.maphunter;

import dev.pterox.maphunter.commands.RmhCommand;
import dev.pterox.maphunter.commands.sub.*;
import dev.pterox.maphunter.event.EventManager;
import dev.pterox.maphunter.leader.LeaderManager;
import dev.pterox.maphunter.listeners.*;
import dev.pterox.maphunter.map.MapManager;
import dev.pterox.maphunter.notification.NotificationManager;
import dev.pterox.maphunter.storage.DatabaseManager;
import dev.pterox.maphunter.storage.LeaderRepository;
import dev.pterox.maphunter.config.MessageConfig;
import dev.pterox.maphunter.integration.BetterTeamsIntegration;
import dev.pterox.maphunter.integration.TeamMemberManager;
import dev.pterox.maphunter.util.ItemUtil;
import dev.pterox.maphunter.util.LogUtil;
import dev.pterox.maphunter.util.MessageUtil;
import dev.pterox.maphunter.util.SchedulerUtil;
import org.bukkit.plugin.java.JavaPlugin;

public class MapHunter extends JavaPlugin {

    private DatabaseManager databaseManager;
    private LeaderRepository leaderRepository;
    private LeaderManager leaderManager;
    private MapManager mapManager;
    private NotificationManager notificationManager;
    private EventManager eventManager;
    private SchedulerUtil schedulerUtil;
    private MessageConfig messageConfig;
    private BetterTeamsIntegration betterTeamsIntegration;
    private TeamMemberManager teamMemberManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        
        // 0. Messages
        messageConfig = new MessageConfig(this);
        LogUtil.init(this);

        // 1. Utilities
        schedulerUtil = new SchedulerUtil(this);
        ItemUtil.init(this);

        // 2. Storage
        databaseManager = new DatabaseManager(this);
        databaseManager.init();
        leaderRepository = new LeaderRepository(this, databaseManager);

        // 3. Leader Manager
        leaderManager = new LeaderManager(leaderRepository);
        leaderManager.loadAll();
        
        // 3.5. Action Bar Task
        new dev.pterox.maphunter.leader.LeaderActionBarTask(this, leaderManager).runTaskTimer(this, 20L, 20L);

        // 4. Map Manager
        mapManager = new MapManager(this, leaderManager, schedulerUtil);
        mapManager.init();

        // 5. Notification & Event Managers
        notificationManager = new NotificationManager(this);
        eventManager = new EventManager(this, mapManager, notificationManager);
        mapManager.setNotificationManager(notificationManager);

        // 5.5. BetterTeams Integration
        betterTeamsIntegration = new BetterTeamsIntegration(this);
        betterTeamsIntegration.checkBetterTeams();
        teamMemberManager = new TeamMemberManager(this, betterTeamsIntegration);
        if (betterTeamsIntegration.isEnabled()) {
            teamMemberManager.loadFromBetterTeams();
        }

        // 6. Commands
        RmhCommand rmhCommand = new RmhCommand(this);
        rmhCommand.registerSubCommand(new LeaderAddCommand(leaderManager));
        rmhCommand.registerSubCommand(new LeaderRemoveCommand(leaderManager));
        rmhCommand.registerSubCommand(new LeaderListCommand(leaderManager));
        rmhCommand.registerSubCommand(new LeaderBackupCommand(leaderManager));
        rmhCommand.registerSubCommand(new LeaderRestoreCommand(leaderManager, mapManager));
        rmhCommand.registerSubCommand(new MapGiveCommand(leaderManager, mapManager));
        rmhCommand.registerSubCommand(new MapRemoveCommand(leaderManager, mapManager));
        rmhCommand.registerSubCommand(new EventStartCommand(eventManager));
        rmhCommand.registerSubCommand(new EventStopCommand(eventManager));
        rmhCommand.registerSubCommand(new EventStatusCommand(eventManager));
        rmhCommand.registerSubCommand(new ReloadCommand());
        rmhCommand.registerSubCommand(new SyncCommand(leaderManager, betterTeamsIntegration));
        rmhCommand.registerSubCommand(new MemberAddCommand(teamMemberManager));
        rmhCommand.registerSubCommand(new MemberRemoveCommand(teamMemberManager));
        rmhCommand.registerPublicSubCommand(new PublicListCommand(leaderManager));

        getCommand("maphunter").setExecutor(rmhCommand);
        getCommand("maphunter").setTabCompleter(rmhCommand);

        // 7. Listeners
        getServer().getPluginManager().registerEvents(new PlayerDeathListener(this, leaderManager, notificationManager), this);
        getServer().getPluginManager().registerEvents(new PlayerDropItemListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerQuitListener(mapManager, eventManager), this);
        getServer().getPluginManager().registerEvents(new CraftItemListener(), this);
        getServer().getPluginManager().registerEvents(new PrepareItemCraftListener(), this);
        getServer().getPluginManager().registerEvents(new VillagerTradeListener(), this);
        getServer().getPluginManager().registerEvents(new EnchantItemListener(eventManager), this);
        getServer().getPluginManager().registerEvents(new dev.pterox.maphunter.listeners.InventoryClickListener(), this);
        getServer().getPluginManager().registerEvents(new dev.pterox.maphunter.listeners.PlayerJoinListener(mapManager), this);

        getServer().getConsoleSender().sendMessage(MessageUtil.color("§b    __  ___               __  __            __"));
        getServer().getConsoleSender().sendMessage(MessageUtil.color("§b   /  |/  /___ _____     / / / /_  ______  / /____  _____"));
        getServer().getConsoleSender().sendMessage(MessageUtil.color("§b  / /|_/ / __ `/ __ \\   / /_/ / / / / __ \\/ __/ _ \\/ ___/"));
        getServer().getConsoleSender().sendMessage(MessageUtil.color("§3 / /  / / /_/ / /_/ /  / __  / /_/ / / / / /_/  __/ /"));
        getServer().getConsoleSender().sendMessage(MessageUtil.color("§3/_/  /_/\\__,_/ .___/  /_/ /_/\\__,_/_/ /_/\\__/\\___/_/"));
        getServer().getConsoleSender().sendMessage(MessageUtil.color("§3            /_/"));
        getServer().getConsoleSender().sendMessage(MessageUtil.color(""));
        
        String scaleStr = getConfig().getString("map.scale", "NORMAL").toUpperCase();
        getServer().getConsoleSender().sendMessage(MessageUtil.color("§a[MapHunter] Map Scale diatur ke: §e" + scaleStr));
        
        getServer().getConsoleSender().sendMessage(MessageUtil.color("§aMapHunter v" + getDescription().getVersion() + " berhasil diaktifkan!"));
    }

    @Override
    public void onDisable() {
        if (eventManager != null && eventManager.isEventActive()) {
            eventManager.stopEvent();
        }
        
        if (databaseManager != null) {
            databaseManager.close();
        }
        
        LogUtil.close();
        getLogger().info("MapHunter disabled successfully!");
    }

    public MessageConfig getMessageConfig() {
        return messageConfig;
    }

    public LeaderManager getLeaderManager() {
        return leaderManager;
    }

    public MapManager getMapManager() {
        return mapManager;
    }

    public EventManager getEventManager() {
        return eventManager;
    }

    public BetterTeamsIntegration getBetterTeamsIntegration() {
        return betterTeamsIntegration;
    }

    public TeamMemberManager getTeamMemberManager() {
        return teamMemberManager;
    }
}
