package eu.kruz3r.messageannouncer.compatibility;

import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.entity.Player;

import java.util.List;

public class Announcer_1_16_R3 implements Announcer {

    @Override
    public void send(Player player, String message) {
        if (message.contains("&#")) {
            String hexColor = message.substring(message.indexOf("&#") + 1, message.indexOf("&#") + 7); // Извлекаем HEX-код
            String text = message.substring(message.indexOf("&#") + 7); // Извлекаем текст
            String json = String.format("{\"text\":\"%s\",\"color\":\"#%s\"}", text, hexColor); // Формируем JSON

            player.spigot().sendMessage(TextComponent.fromLegacyText(json));
        } else {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
        }
    }

    @Override
    public void send(Player player, List<String> messages) {
        for (String message : messages) {
            send(player, message);
        }
    }
}