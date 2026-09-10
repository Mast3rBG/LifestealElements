package master.com.element;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;
import java.util.stream.Collectors;

public final class LifestealElement extends JavaPlugin {

    private static LifestealElement instance;

    private CooldownManager cooldownManager;
    private ElementGUI      elementGUI;
    private ElementVoucher  elementVoucher;
    private AbilityHandler  abilityHandler;

    private final Set<UUID> unlimitedPlayers = new HashSet<>();

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        reloadConfig();

        if (!LicenseManager.validate(this)) return;

        ElementManager.init(this);

        this.cooldownManager = new CooldownManager();
        this.elementGUI      = new ElementGUI();
        this.elementVoucher  = new ElementVoucher(this);
        this.abilityHandler  = new AbilityHandler(this);

        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
        getServer().getPluginManager().registerEvents(elementVoucher, this);

        var cmd = getCommand("elements");
        if (cmd != null) {
            cmd.setExecutor((sender, c, label, args) -> {
                if (!(sender instanceof Player p)) {
                    sender.sendMessage("§cOnly players can use this command.");
                    return true;
                }
                if (!p.hasPermission("elements.staff") && !p.hasPermission("elements.admin")) {
                    p.sendMessage("§cYou do not have permission.");
                    return true;
                }
                return handleCommand(p, args);
            });
            cmd.setTabCompleter((TabCompleter) (sender, c, alias, args) -> buildTabComplete(args));
        }

        getLogger().info("LifestealElements v2 enabled! " + Element.values().length + " elements loaded.");
    }

    @Override
    public void onDisable() {
        ElementManager.save();
        getLogger().info("LifestealElements v2 disabled. Player data saved.");
    }

    private boolean handleCommand(Player p, String[] args) {
        if (args.length == 0) {
            elementGUI.openFor(p);
            return true;
        }

        return switch (args[0].toLowerCase()) {
            case "give" -> {
                if (args.length < 3) { p.sendMessage("§cUsage: /elements give <player> <element>"); yield true; }
                Player target = Bukkit.getPlayer(args[1]);
                if (target == null) { p.sendMessage("§cPlayer not found."); yield true; }
                Element el = Element.fromDisplay(args[2]);
                if (el == null) { p.sendMessage("§cInvalid element. Options: " + Element.getAllNames()); yield true; }
                for (int i = 0; i < target.getInventory().getSize(); i++) {
                    ItemStack it = target.getInventory().getItem(i);
                    if (it != null && Element.fromItem(it) != null) target.getInventory().setItem(i, null);
                }
                ElementManager.setElement(target, el);
                target.getInventory().addItem(el.buildItem(this));
                target.sendMessage(el.getColor() + "§l" + el.getDisplay() + " element §7assigned by §b" + p.getName());
                p.sendMessage("§aAssigned " + el.getColor() + "§l" + el.getDisplay() + " §ato §b" + target.getName());
                yield true;
            }
            case "remove" -> {
                if (args.length < 2) { p.sendMessage("§cUsage: /elements remove <player>"); yield true; }
                Player target = Bukkit.getPlayer(args[1]);
                if (target == null) { p.sendMessage("§cPlayer not found."); yield true; }
                ElementManager.clearElement(target);
                for (int i = 0; i < target.getInventory().getSize(); i++) {
                    ItemStack it = target.getInventory().getItem(i);
                    if (it != null && Element.fromItem(it) != null) target.getInventory().setItem(i, null);
                }
                target.sendMessage("§cYour element was removed by §b" + p.getName());
                p.sendMessage("§cRemoved element from §b" + target.getName());
                yield true;
            }
            case "voucher" -> {
                if (args.length < 2) { p.sendMessage("§cUsage: /elements voucher <element>"); yield true; }
                Element el = Element.fromDisplay(args[1]);
                if (el == null) { p.sendMessage("§cInvalid element."); yield true; }
                p.getInventory().addItem(ElementVoucher.createVoucher(el));
                p.sendMessage("§aGiven voucher for " + el.getColor() + "§l" + el.getDisplay());
                yield true;
            }
            case "list" -> {
                p.sendMessage("§6§lAvailable Elements: §r" + Element.getAllNames());
                yield true;
            }
            case "reload" -> {
                reloadConfig();
                p.sendMessage("§aElements config reloaded.");
                yield true;
            }
            case "unlimited" -> {
                unlimitedPlayers.add(p.getUniqueId());
                p.sendMessage("§aYou are now in §6§lUNLIMITED MODE §a— no cooldowns.");
                yield true;
            }
            case "limited" -> {
                unlimitedPlayers.remove(p.getUniqueId());
                p.sendMessage("§aBack to §cLIMITED MODE §a— cooldowns active.");
                yield true;
            }
            default -> {
                p.sendMessage("§7Usage: /elements [give|remove|voucher|list|reload|unlimited|limited]");
                yield true;
            }
        };
    }

    private List<String> buildTabComplete(String[] args) {
        if (args.length == 1) return List.of("give", "remove", "voucher", "list", "reload", "unlimited", "limited");
        if (args.length == 2 && (args[0].equalsIgnoreCase("give") || args[0].equalsIgnoreCase("remove")))
            return Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList());
        if (args.length == 3 && args[0].equalsIgnoreCase("give"))
            return Arrays.stream(Element.values()).map(Element::getDisplay).collect(Collectors.toList());
        if (args.length == 2 && args[0].equalsIgnoreCase("voucher"))
            return Arrays.stream(Element.values()).map(Element::getDisplay).collect(Collectors.toList());
        return List.of();
    }

    public static boolean canBuild(Player player) {
        try {
            WorldGuardPlugin wg = WorldGuardPlugin.inst();
            if (wg == null) return true;
            RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
            RegionQuery query = container.createQuery();
            return query.testState(BukkitAdapter.adapt(player.getLocation()), wg.wrapPlayer(player), Flags.BUILD);
        } catch (Exception ignored) { return true; }
    }

    public static LifestealElement getInstance()    { return instance; }
    public CooldownManager getCooldownManager()     { return cooldownManager; }
    public ElementGUI      getElementGUI()          { return elementGUI; }
    public AbilityHandler  getAbilityHandler()      { return abilityHandler; }
    public boolean         isUnlimited(Player p)    { return unlimitedPlayers.contains(p.getUniqueId()); }
}
