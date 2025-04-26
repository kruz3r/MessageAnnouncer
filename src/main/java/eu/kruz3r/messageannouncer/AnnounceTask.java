package eu.kruz3r.messageannouncer;

import java.util.Iterator;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class AnnounceTask implements Runnable {
    private final List<String> announcement;

    public AnnounceTask(List<String> announcement) {
        this.announcement = announcement;
    }

    public void run() {
        Iterator var2 = Bukkit.getServer().getOnlinePlayers().iterator();

        while(var2.hasNext()) {
            Player p = (Player)var2.next();
            MessageAnnouncer.sendAnnouncement(p, this.announcement);
        }

    }
}
