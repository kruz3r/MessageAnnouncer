package eu.kruz3r.messageannouncer;

import java.util.List;
import java.util.Random;

public class IntervalTask implements Runnable {
    private MessageAnnouncer plugin;

    public IntervalTask(MessageAnnouncer instance) {
        this.plugin = instance;
    }

    public void run() {
        List<String> active = Announcements.getIntervalAnnouncements();
        if (active != null && !active.isEmpty()) {
            String id = null;
            if (MessageAnnouncer.random) {
                int s = (new Random()).nextInt(active.size());
                if (MessageAnnouncer.last == s) {
                    s = (new Random()).nextInt(active.size());
                }

                MessageAnnouncer.last = s;
                id = (String)active.get(s);
            } else if (MessageAnnouncer.last < active.size()) {
                ++MessageAnnouncer.last;
                id = (String)active.get(MessageAnnouncer.last - 1);
            } else {
                MessageAnnouncer.last = 1;
                id = (String)active.get(0);
            }

            if (id != null) {
                List<String> send = Announcements.getAnnouncement(id);
                if (send != null && !send.isEmpty()) {
                    this.plugin.getServer().getScheduler().runTask(this.plugin, new AnnounceTask(send));
                }

            }
        }
    }
}