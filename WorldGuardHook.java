package master.com.element;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class WorldGuardHook {
    public static boolean canUseAbility(Player player, Location loc) {
        try {
            WorldGuardPlugin wg = WorldGuardPlugin.inst();
            if (wg == null) return true;
            RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
            RegionQuery query = container.createQuery();
            return query.testState(BukkitAdapter.adapt(loc), wg.wrapPlayer(player), Flags.BUILD);
        } catch (Exception ignored) {
            return true;
        }
    }
}
