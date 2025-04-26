package eu.kruz3r.messageannouncer;

import eu.kruz3r.messageannouncer.compatibility.Announcer;
import eu.kruz3r.messageannouncer.compatibility.Announcer_1_16_R3;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.logging.Level;

public class MessageAnnouncer extends JavaPlugin implements Listener {
    protected static int announceInterval;
    protected static int announcementLength;
    protected static boolean random;
    protected static BukkitTask iTask;
    private final MessageAnnouncerCommands commands = new MessageAnnouncerCommands(this);
    private static Announcer announcer;
    private static boolean placeholderHook;
    protected static int last;
    private static boolean soundEnabled;
    private static Sound sound;
    private static int volume;
    private static int pitch;

    @Override
    public void onEnable() {
        if (this.setupCompatibility()) {
            this.loadCfg();
            this.loadSettings();
            placeholderHook = Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI");
            if (placeholderHook) {
                this.getLogger().info("PlaceholderAPI integration was successful!");
            }
            if (this.getConfig().getBoolean("announcer_enabled")) {
                this.startAnnouncements();
            }
            this.getCommand("msa").setExecutor(this.commands);
        }
    }

    @Override
    public void onDisable() {
        this.stopAnnouncements();
        Announcements.unload();
        this.getServer().getScheduler().cancelTasks(this);
    }

    private boolean setupCompatibility() {
        try {
            announcer = new Announcer_1_16_R3();
            return true;
        } catch (Exception e) {
            this.getLogger().log(Level.SEVERE, "Failed to setup compatibility for 1.16.5!", e);
            return false;
        }
    }

    private void loadSettings() {
        random = this.getConfig().getBoolean("announcer_random");
        announceInterval = this.getConfig().getInt("announce_interval");
        announcementLength = this.getConfig().getInt("announcement_length");
        this.loadAnnouncements();
        this.loadSound();
    }

    private void loadSound() {
        if (!this.getConfig().getBoolean("sound.enabled")) {
            soundEnabled = false;
        } else {
            String s = this.getConfig().getString("sound.sound_name").toUpperCase();
            try {
                sound = Sound.valueOf(s);
                soundEnabled = true;
            } catch (Exception e) {
                this.getLogger().warning("Your sound " + s + " is invalid!");
                this.getLogger().info("Valid sound names can be found at the following link:");
                this.getLogger().info("https://hub.spigotmc.org/javadocs/spigot/org/bukkit/Sound.html");
                this.getLogger().info("Sound on message will be disabled!");
                soundEnabled = false;
            }
            volume = this.getConfig().getInt("sound.volume");
            pitch = this.getConfig().getInt("sound.pitch");
        }
    }

    private int loadAnnouncements() {
        Set<String> keys;
        Announcements.unload();
        if (this.getConfig().isConfigurationSection("announcements") && (keys = this.getConfig().getConfigurationSection("announcements").getKeys(false)) != null && !keys.isEmpty()) {
            for (String key : keys) {
                if (!this.getConfig().isList("announcements." + key)) continue;
                Announcements.addAnnouncement(key, this.getConfig().getStringList("announcements." + key));
            }
        }
        Announcements.setIntervalAnnouncements(this.getConfig().getStringList("interval_announcement_list"));
        return Announcements.getSize();
    }

    private void loadCfg() {
        FileConfiguration c = this.getConfig();
        c.options().header("MessageAnnouncer v" + this.getDescription().getVersion() + " Main configuration");
        c.addDefault("announcer_enabled", true);
        c.addDefault("announcer_random", true);
        c.addDefault("announce_interval", 60);
        c.addDefault("sound.enabled", true);
        c.addDefault("sound.sound_name", "BLOCK_NOTE_BLOCK_PLING");
        c.addDefault("sound.volume", 10);
        c.addDefault("sound.pitch", 1);
        c.addDefault("interval_announcement_list", Arrays.asList("default", "vote"));
        if (!c.isConfigurationSection("announcements")) {
            c.addDefault("announcements.default", Arrays.asList("&#FFD700Золотой текст", "&#00FF00Зелёный текст"));
            c.addDefault("announcements.vote", Arrays.asList("&#FF0000Красный текст", "&#0000FFСиний текст"));
        }
        c.options().copyDefaults(true);
        this.saveConfig();
    }

    protected void relCfg() {
        this.stopAnnouncements();
        this.reloadConfig();
        this.saveConfig();
        this.loadSettings();
        if (this.getConfig().getBoolean("announcer_enabled")) {
            this.startAnnouncements();
        }
    }

    protected void startAnnouncements() {
        if (iTask == null) {
            iTask = this.getServer().getScheduler().runTaskTimerAsynchronously(this, new IntervalTask(this), 20L * (long) announceInterval, 20L * (long) announceInterval);
        } else {
            iTask.cancel();
            iTask = null;
            iTask = this.getServer().getScheduler().runTaskTimerAsynchronously(this, new IntervalTask(this), 20L * (long) announceInterval, 20L * (long) announceInterval);
        }
    }

    protected void stopAnnouncements() {
        if (iTask != null) {
            iTask.cancel();
            iTask = null;
        }
    }

    protected static void sendPlayerSpecificAnnouncement(Player p, List<String> lines, Player placeholders) {
        if (lines != null && !lines.isEmpty()) {
            if (placeholderHook) {
                lines = PlaceholderAPI.setPlaceholders(placeholders, lines);
            }
            if (lines != null) {
                for (String line : lines) {
                    line = hex(line); // Обрабатываем HEX-цвета
                    if (line.contains("%player%")) {
                        line = line.replace("%player%", placeholders.getName());
                    }
                    if (line.contains("%displayname%")) {
                        line = line.replace("%displayname%", placeholders.getDisplayName());
                    }
                    if (line.contains("%online%")) {
                        line = line.replace("%online%", String.valueOf(Bukkit.getOnlinePlayers().size()));
                    }
                    if (line.startsWith("[text]")) {
                        line = line.replace("[text]", "");
                        p.sendMessage(line);
                        continue;
                    }
                    if (!line.contains("&&")) {
                        announcer.send(p, line);
                        continue;
                    }
                    String[] parts = line.split("&&");
                    ArrayList<String> components = new ArrayList<>(Arrays.asList(parts));
                    announcer.send(p, components);
                }
                if (soundEnabled && sound != null) {
                    p.playSound(p.getLocation(), sound, volume, pitch);
                }
            }
        }
    }

    protected static void sendAnnouncement(Player p, List<String> lines) {
        if (lines != null && !lines.isEmpty()) {
            if (placeholderHook) {
                lines = PlaceholderAPI.setPlaceholders(p, lines);
            }
            if (lines != null) {
                for (String line : lines) {
                    line = hex(line);
                    if (line.contains("%player%")) {
                        line = line.replace("%player%", p.getName());
                    }
                    if (line.contains("%displayname%")) {
                        line = line.replace("%displayname%", p.getDisplayName());
                    }
                    if (line.contains("%online%")) {
                        line = line.replace("%online%", String.valueOf(Bukkit.getOnlinePlayers().size()));
                    }
                    if (line.startsWith("[text]")) {
                        line = line.replace("[text]", "");
                        p.sendMessage(line);
                        continue;
                    }
                    if (!line.contains("&&")) {
                        announcer.send(p, line);
                        continue;
                    }
                    String[] parts = line.split("&&");
                    ArrayList<String> components = new ArrayList<>(Arrays.asList(parts));
                    announcer.send(p, components);
                }
                if (soundEnabled && sound != null) {
                    p.playSound(p.getLocation(), sound, volume, pitch);
                }
            }
        }
    }
    public static String hex(String from) {
        Pattern pattern = Pattern.compile("&#[a-fA-F0-9]{6}");
        Matcher matcher = pattern.matcher(from);
        while (matcher.find()) {
            String hexCode = from.substring(matcher.start(), matcher.end());
            String replaceSharp = hexCode.replace("&#", "x");
            char[] ch = replaceSharp.toCharArray();
            StringBuilder builder = new StringBuilder();
            for (char c : ch) builder.append("&").append(c);
            from = from.replace(hexCode, builder.toString());
            matcher = pattern.matcher(from);
        }
        return ChatColor.translateAlternateColorCodes('&', from);
    }
}