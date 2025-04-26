package eu.kruz3r.messageannouncer.compatibility;

import org.bukkit.entity.Player;

import java.util.List;

public interface Announcer {
        public void send(Player var1, String var2);

        public void send(Player var1, List<String> var2);
    }
