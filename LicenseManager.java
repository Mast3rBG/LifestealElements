package master.com.element;

import org.bukkit.plugin.java.JavaPlugin;

public class LicenseManager {
    private static final String VALID_PREFIX = "ME-";

    public static boolean validate(JavaPlugin plugin) {
        String license = plugin.getConfig().getString("license", "");
        if (license.isBlank() || license.equals("PASTE-YOUR-LICENSE-HERE!")) {
            plugin.getLogger().severe("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            plugin.getLogger().severe(" Elements — No license configured!");
            plugin.getLogger().severe(" Set 'license' in config.yml");
            plugin.getLogger().severe("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            plugin.getServer().getPluginManager().disablePlugin(plugin);
            return false;
        }
        if (!license.startsWith(VALID_PREFIX)) {
            plugin.getLogger().severe("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            plugin.getLogger().severe(" Elements — Invalid license key!");
            plugin.getLogger().severe("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            plugin.getServer().getPluginManager().disablePlugin(plugin);
            return false;
        }
        plugin.getLogger().info("License validated.");
        return true;
    }
}
