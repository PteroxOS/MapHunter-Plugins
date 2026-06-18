package dev.pterox.maphunter.util;

import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

public class SchedulerUtil {

    private final Plugin plugin;

    public SchedulerUtil(Plugin plugin) {
        this.plugin = plugin;
    }

    public BukkitTask runTaskTimer(Runnable task, long delay, long period) {
        return plugin.getServer().getScheduler().runTaskTimer(plugin, task, delay, period);
    }

    public BukkitTask runTaskAsynchronously(Runnable task) {
        return plugin.getServer().getScheduler().runTaskAsynchronously(plugin, task);
    }

    public void runTask(Runnable task) {
        plugin.getServer().getScheduler().runTask(plugin, task);
    }
}
