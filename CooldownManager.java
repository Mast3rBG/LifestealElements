package master.com.element;

import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CooldownManager {

    private final Map<UUID, Map<String, Long>> data = new HashMap<>();

    public boolean isOnCooldown(UUID uuid, String key) {
        Map<String, Long> map = data.get(uuid);
        if (map == null) return false;
        Long t = map.get(key);
        if (t == null) return false;
        if (System.currentTimeMillis() > t) { map.remove(key); return false; }
        return true;
    }

    public long timeLeftMs(UUID uuid, String key) {
        Map<String, Long> map = data.get(uuid);
        if (map == null) return 0;
        Long t = map.get(key);
        if (t == null) return 0;
        long left = t - System.currentTimeMillis();
        return Math.max(0, left);
    }

    public void setCooldown(UUID uuid, String key, long millis) {
        data.computeIfAbsent(uuid, k -> new HashMap<>()).put(key, System.currentTimeMillis() + millis);
    }

    public void clearCooldown(UUID uuid, String key) {
        Map<String, Long> map = data.get(uuid);
        if (map != null) map.remove(key);
    }

    public void sendCooldownBar(Player p, Element el, String abilityName, long leftMs) {
        double secs = leftMs / 1000.0;
        String formatted = secs < 10 ? String.format("%.1f", secs) : String.valueOf((int) Math.ceil(secs));
        p.sendActionBar(Component.text(
            el.getColor() + "§l" + el.getDisplay() + " §8| §c" + abilityName + " §7cooldown: §f" + formatted + "s"
        ));
    }
}
