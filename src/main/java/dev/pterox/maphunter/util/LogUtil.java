package dev.pterox.maphunter.util;

import dev.pterox.maphunter.MapHunter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class LogUtil {

    private static File logFile;
    private static PrintWriter writer;

    public static void init(MapHunter plugin) {
        File logsDir = new File(plugin.getDataFolder(), "logs");
        if (!logsDir.exists()) {
            logsDir.mkdirs();
        }
        logFile = new File(logsDir, "maphunter.log");
        try {
            writer = new PrintWriter(new FileWriter(logFile, true), true);
            log("=== MapHunter Log Started ===");
        } catch (IOException e) {
            plugin.getLogger().warning("Could not create log file: " + e.getMessage());
        }
    }

    public static void log(String message) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String logMessage = "[" + timestamp + "] " + message;
        
        if (writer != null) {
            writer.println(logMessage);
        }
        
        System.out.println("[MapHunter Log] " + logMessage);
    }

    public static void logLeaderDeath(String playerName, String clanName, String killerName) {
        log("LEADER DEATH: " + playerName + " (clan: " + clanName + ") killed by " + (killerName != null ? killerName : "unknown"));
    }

    public static void logMapTransfer(String from, String to, String clanName) {
        log("MAP TRANSFER: " + from + " -> " + to + " (clan: " + clanName + ")");
    }

    public static void logAutoWin(String winnerName, String clanName) {
        log("AUTO WIN: " + winnerName + " (clan: " + clanName + ") wins the event!");
    }

    public static void logCountdownStart(String clanName, int seconds) {
        log("COUNTDOWN START: " + clanName + " offline for " + seconds + " seconds");
    }

    public static void logCountdownExpired(String clanName) {
        log("COUNTDOWN EXPIRED: " + clanName + " leader did not return");
    }

    public static void logCountdownCancelled(String clanName) {
        log("COUNTDOWN CANCELLED: " + clanName + " leader returned");
    }

    public static void logEventStart() {
        log("EVENT STARTED");
    }

    public static void logEventStop() {
        log("EVENT STOPPED");
    }

    public static void logRestore(String leaderName, String clanName) {
        log("LEADER RESTORE: " + leaderName + " (clan: " + clanName + ") restored by admin");
    }

    public static void logRemoveLeader(String playerName, String clanName) {
        log("LEADER REMOVED: " + playerName + " (clan: " + clanName + ")");
    }

    public static void logAddLeader(String playerName, String clanName, String color) {
        log("LEADER ADDED: " + playerName + " (clan: " + clanName + ", color: " + color + ")");
    }

    public static void logAddBackup(String playerName, String clanName) {
        log("BACKUP ADDED: " + playerName + " (clan: " + clanName + ")");
    }

    public static void close() {
        if (writer != null) {
            log("=== MapHunter Log Ended ===");
            writer.close();
        }
    }
}
