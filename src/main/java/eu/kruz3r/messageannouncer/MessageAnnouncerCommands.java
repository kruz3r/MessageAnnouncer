package eu.kruz3r.messageannouncer;

import java.util.List;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class MessageAnnouncerCommands
        implements CommandExecutor {
    MessageAnnouncer plugin;

    public MessageAnnouncerCommands(MessageAnnouncer i) {
        this.plugin = i;
    }

    public void sms(CommandSender s, String msg) {
        s.sendMessage(ChatColor.translateAlternateColorCodes('&', msg));
    }

    public boolean onCommand(CommandSender s, Command cmd, String label, String[] args) {
        if (args.length == 0) {
            this.sms(s, "&8&m-----------------------------------------------------");
            this.sms(s, "&c&lM&cessage&7&lA&7nnouncer &f&o" + this.plugin.getDescription().getVersion());
            this.sms(s, "&7Created by &fgithub.com/kruz3r");
            this.sms(s, "&8&m-----------------------------------------------------");
            return true;
        }
        if (s instanceof Player && !s.hasPermission("messageannouncer.admin")) {
            this.sms(s, "&cYou don't have permission to do that!");
            return true;
        }
        if (args[0].equalsIgnoreCase("reload")) {
            this.plugin.relCfg();
            this.sms(s, "&8&m-----------------------------------------------------");
            this.sms(s, "&c&lM&cessage&7&lA&7nnouncer &bconfiguration reloaded!");
            this.sms(s, "&8&m-----------------------------------------------------");
            return true;
        }
        if (args[0].equalsIgnoreCase("help")) {
            this.sms(s, "&8&m-----------------------------------------------------");
            this.sms(s, "&c&lM&cessage&7&lA&7nnouncer &fHelp");
            this.sms(s, "&c/msa start");
            this.sms(s, "&fStart interval announcements");
            this.sms(s, "&c/msa stop");
            this.sms(s, "&fStop interval announcements");
            this.sms(s, "&c/msa list <active/all>");
            this.sms(s, "&fList active/all announcements ");
            this.sms(s, "&c/msa send <player> <announcement id>");
            this.sms(s, "&fSend an announcement to a player ");
            this.sms(s, "&c/msa announce <announcement id> (player to parse placeholders for)");
            this.sms(s, "&fSend an announcement to all players ");
            this.sms(s, "&c/msa reload");
            this.sms(s, "&fReload &c&lM&cessage&7&lA&7nnouncer");
            this.sms(s, "&8&m-----------------------------------------------------");
            return true;
        }
        if (args[0].equalsIgnoreCase("list")) {
            if (args.length != 2) {
                this.sms(s, "&cIncorrect usage! &7/msa list <active/all>");
                return true;
            }
            String arg = args[1];
            if (arg.equalsIgnoreCase("active")) {
                List<String> active = Announcements.getIntervalAnnouncements();
                if (active == null || active.isEmpty()) {
                    this.sms(s, "&cThere are no active announcements loaded!");
                    return true;
                }
                this.sms(s, "&7Active announcements: &f" + active.size());
                this.sms(s, active.toString().replace("[", "").replace(",", "&e,&f").replace("]", ""));
                return true;
            }
            if (arg.equalsIgnoreCase("all")) {
                Set<String> all = Announcements.getAllAnnouncementIds();
                if (all == null || all.isEmpty()) {
                    this.sms(s, "&cThere are no announcements loaded!");
                    return true;
                }
                this.sms(s, "&7Loaded announcements: &f" + all.size());
                this.sms(s, all.toString().replace("[", "").replace(",", "&e,&f").replace("]", ""));
                return true;
            }
            this.sms(s, "&cIncorrect usage! &7/msa list <active/all>");
            return true;
        }
        if (args[0].equalsIgnoreCase("send")) {
            if (args.length != 3) {
                this.sms(s, "&cIncorrect usage! &7/msa send <player> <announcement id>");
                return true;
            }
            String t = args[1];
            Player target = Bukkit.getPlayer(t);
            if (t == null) {
                this.sms(s, String.valueOf(t) + " &cis not online!");
                return true;
            }
            String id = args[2];
            if (Announcements.getAnnouncement(id) == null) {
                this.sms(s, "&cThere is no announcement with the id: &f" + id);
                return true;
            }
            List<String> message = Announcements.getAnnouncement(id);
            MessageAnnouncer.sendAnnouncement(target, message);
            this.sms(s, "&aAnnouncement &f" + id + " &asent to &f" + target.getName());
            return true;
        }
        if (args[0].equalsIgnoreCase("announce")) {
            if (args.length < 2) {
                this.sms(s, "&cIncorrect usage! &7/msa announce <announcement id> (player to parse placeholders for)");
                return true;
            }
            String id = args[1];
            if (Announcements.getAnnouncement(id) == null) {
                this.sms(s, "&cThere is no announcement with the id: &f" + id);
                return true;
            }
            List<String> message = Announcements.getAnnouncement(id);
            Player target = null;
            if (args.length == 3) {
                target = Bukkit.getPlayer(args[2]);
            }
            if (target != null) {
                for (Player online : Bukkit.getOnlinePlayers()) {
                    MessageAnnouncer.sendPlayerSpecificAnnouncement(online, message, target);
                }
                this.sms(s, "&aPlayer specific announcement &f" + id + " &asent to all players");
            } else {
                for (Player online : Bukkit.getOnlinePlayers()) {
                    MessageAnnouncer.sendAnnouncement(online, message);
                }
                this.sms(s, "&aAnnouncement &f" + id + " &asent to all players");
            }
            return true;
        }
        if (args[0].equalsIgnoreCase("stop")) {
            if (MessageAnnouncer.iTask == null) {
                this.sms(s, "&cThere are interval announcements currently running!");
                return true;
            }
            this.plugin.stopAnnouncements();
            Bukkit.getScheduler().cancelTasks(this.plugin);
            this.sms(s, "&bInterval announcements have been stopped");
            return true;
        }
        if (args[0].equalsIgnoreCase("start")) {
            if (MessageAnnouncer.iTask != null) {
                this.sms(s, "&cInterval announcements are already running!");
                return true;
            }
            this.plugin.startAnnouncements();
            this.sms(s, "&bInterval announcements have been started");
            return true;
        }
        this.sms(s, "&cIncorrect usage!");
        return true;
    }
}
