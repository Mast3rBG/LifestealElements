package master.com.element;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ElementManager {

    private static final Map<UUID, Element> playerElements = new HashMap<>();
    private static File                     dataFile;
    private static FileConfiguration        dataConfig;

    public static void init(LifestealElement plugin) {
        dataFile = new File(plugin.getDataFolder(), "playerdata.yml");
        if (!dataFile.exists()) {
            try { dataFile.createNewFile(); } catch (IOException e) { e.printStackTrace(); }
        }
        dataConfig = YamlConfiguration.loadConfiguration(dataFile);

        if (dataConfig.getConfigurationSection("elements") != null) {
            for (String uuidStr : dataConfig.getConfigurationSection("elements").getKeys(false)) {
                try {
                    UUID uuid = UUID.fromString(uuidStr);
                    String name = dataConfig.getString("elements." + uuidStr);
                    if (name != null) {
                        try { playerElements.put(uuid, Element.valueOf(name)); }
                        catch (IllegalArgumentException ignored) {}
                    }
                } catch (IllegalArgumentException ignored) {}
            }
        }
        plugin.getLogger().info("Loaded " + playerElements.size() + " player element assignments.");
    }

    public static void save() {
        if (dataConfig == null) return;
        for (Map.Entry<UUID, Element> e : playerElements.entrySet()) {
            dataConfig.set("elements." + e.getKey(), e.getValue().name());
        }
        try { dataConfig.save(dataFile); }
        catch (IOException e) { e.printStackTrace(); }
    }

    public static void setElement(Player player, Element element) {
        if (player == null) return;
        if (element == null) playerElements.remove(player.getUniqueId());
        else                 playerElements.put(player.getUniqueId(), element);
        save();
    }

    public static Element getElement(Player player) {
        if (player == null) return null;
        return playerElements.get(player.getUniqueId());
    }

    public static void clearElement(Player player) {
        if (player == null) return;
        playerElements.remove(player.getUniqueId());
        save();
    }

    public static boolean hasElement(Player player) {
        return player != null && playerElements.containsKey(player.getUniqueId());
    }
}
