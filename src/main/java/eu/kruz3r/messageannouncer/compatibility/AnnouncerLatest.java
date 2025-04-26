package eu.kruz3r.messageannouncer.compatibility;

import eu.kruz3r.messageannouncer.MessageAnnouncer;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.stream.Collectors;

public class AnnouncerLatest implements Announcer {
    private final MessageAnnouncer plugin;

    public AnnouncerLatest(MessageAnnouncer plugin) {
        this.plugin = plugin;
    }

    @Override
    public void send(Player p, String json) {
        String parsedJson = parseHexColors(json);
        BaseComponent[] component = ComponentSerializer.parse(parsedJson);
        p.spigot().sendMessage(component);
    }

    @Override
    public void send(Player p, List<String> json) {
        if (json != null && !json.isEmpty()) {
            List<BaseComponent[]> components = json.stream()
                    .map(this::parseHexColors)
                    .map(ComponentSerializer::parse)
                    .collect(Collectors.toList());

            for (BaseComponent[] component : components) {
                p.spigot().sendMessage(component);
            }
        }
    }

    private String parseHexColors(String message) {
        return ChatColor.translateAlternateColorCodes('&', message)
                .replace("&#", "#");
    }
}