package eu.kruz3r.messageannouncer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Announcements {
    private static List<String> intervalAnnouncements = new ArrayList();
    private static Map<String, List<String>> loaded = new HashMap();

    protected static List<String> getIntervalAnnouncements() {
        return intervalAnnouncements;
    }

    protected static void setIntervalAnnouncements(List<String> intervalAnnouncements) {
        Announcements.intervalAnnouncements = intervalAnnouncements;
    }

    protected static void unload() {
        loaded = null;
        intervalAnnouncements = null;
    }

    protected static boolean addAnnouncement(String id, List<String> message) {
        if (loaded == null) {
            loaded = new HashMap();
        }

        if (id != null && message != null) {
            loaded.put(id, message);
            return true;
        } else {
            return false;
        }
    }

    protected static boolean removeAnnouncement(String id) {
        if (id != null && loaded != null) {
            return loaded.remove(id) != null;
        } else {
            return false;
        }
    }

    public static Set<String> getAllAnnouncementIds() {
        return loaded != null ? loaded.keySet() : null;
    }

    protected static List<String> getAnnouncement(String id) {
        if (loaded != null && !loaded.isEmpty()) {
            return !loaded.containsKey(id) ? null : (List)loaded.get(id);
        } else {
            return null;
        }
    }

    public static int getSize() {
        return loaded != null ? loaded.size() : 0;
    }
}
