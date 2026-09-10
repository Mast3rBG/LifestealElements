package master.com.element;

import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerItemBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class PlayerListener implements Listener {

    private final LifestealElement plugin;
    private final AbilityHandler   abilityHandler;
    private final ElementGUI       gui;

    public PlayerListener(LifestealElement plugin) {
        this.plugin         = plugin;
        this.abilityHandler = plugin.getAbilityHandler();
        this.gui            = plugin.getElementGUI();
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        Element el = ElementManager.getElement(p);
        if (el == null) return;

        boolean hasItem = false;
        for (ItemStack item : p.getInventory().getContents()) {
            if (item != null && Element.fromItem(item) == el) { hasItem = true; break; }
        }
        if (!hasItem) {
            p.getInventory().addItem(el.buildItem(plugin));
            p.sendMessage(el.getColor() + "§lElement Restored §8» §7Your " + el.getColor() + "§l" + el.getDisplay() + " §7item was restored.");
        }
        applyPassive(p, el);
    }

    @EventHandler
    public void onHeldChange(PlayerItemHeldEvent e) {
        Player p = e.getPlayer();
        Element el = ElementManager.getElement(p);
        if (el == null) { clearPassives(p); return; }

        ItemStack next = p.getInventory().getItem(e.getNewSlot());
        if (Element.fromItem(next) == el) {
            applyPassive(p, el);
        } else {
            clearPassives(p);
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        if (e.getHand() != EquipmentSlot.HAND) return;
        Player p = e.getPlayer();
        Element el = ElementManager.getElement(p);
        if (el == null) return;

        ItemStack main = p.getInventory().getItemInMainHand();
        if (Element.fromItem(main) != el) return;

        if (!WorldGuardHook.canUseAbility(p, p.getLocation())) {
            p.sendMessage("§cYou cannot use abilities in a protected region!"); return;
        }

        boolean unlimited = plugin.isUnlimited(p);
        String a1key = el.name() + "-A1";
        String a2key = el.name() + "-A2";
        CooldownManager cd = plugin.getCooldownManager();

        Action action = e.getAction();

        if (action == Action.LEFT_CLICK_AIR || action == Action.LEFT_CLICK_BLOCK) {
            if (!unlimited && cd.isOnCooldown(p.getUniqueId(), a1key)) {
                cd.sendCooldownBar(p, el, "Ability 1", cd.timeLeftMs(p.getUniqueId(), a1key));
                return;
            }
            e.setCancelled(true);
            abilityHandler.activate1(p, el);
            if (!unlimited) {
                long cooldown = plugin.getConfig().getLong("cooldowns." + el.name().toLowerCase() + ".ability1", 20) * 1000L;
                cd.setCooldown(p.getUniqueId(), a1key, cooldown);
            }
        }

        if (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK) {
            if (!unlimited && cd.isOnCooldown(p.getUniqueId(), a2key)) {
                cd.sendCooldownBar(p, el, "Ability 2", cd.timeLeftMs(p.getUniqueId(), a2key));
                return;
            }
            e.setCancelled(true);
            abilityHandler.activate2(p, el);
            if (!unlimited) {
                long cooldown = plugin.getConfig().getLong("cooldowns." + el.name().toLowerCase() + ".ability2", 25) * 1000L;
                cd.setCooldown(p.getUniqueId(), a2key, cooldown);
            }
        }
    }

    @EventHandler
    public void onGuiClick(InventoryClickEvent e) {
        gui.handleClick(e);
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent e) {
        Player p = e.getPlayer();
        Element el = ElementManager.getElement(p);
        if (el == null) return;
        if (Element.fromItem(e.getItemDrop().getItemStack()) == el) {
            e.setCancelled(true);
            p.sendActionBar(Component.text(el.getColor() + "§lElement item §7cannot be dropped!"));
        }
    }

    @EventHandler
    public void onItemBreak(PlayerItemBreakEvent e) {
        Player p = e.getPlayer();
        Element el = ElementManager.getElement(p);
        if (el == null) return;
        if (Element.fromItem(e.getBrokenItem()) != el) return;
        org.bukkit.Bukkit.getScheduler().runTaskLater(plugin, () -> {
            p.getInventory().setItemInMainHand(el.buildItem(plugin));
            p.sendActionBar(Component.text(el.getColor() + "§lElement item §7restored."));
        }, 1L);
    }

    private void applyPassive(Player p, Element el) {
        if (p == null || el == null) return;
        switch (el) {
            case FLAME     -> p.addPotionEffect(inf(PotionEffectType.FIRE_RESISTANCE, 0));
            case LIGHTNING -> p.addPotionEffect(inf(PotionEffectType.SPEED, 1));
            case WATER     -> {
                p.addPotionEffect(inf(PotionEffectType.WATER_BREATHING, 0));
                p.addPotionEffect(inf(PotionEffectType.DOLPHINS_GRACE, 0));
            }
            case EARTH     -> {
                p.addPotionEffect(inf(PotionEffectType.HASTE, 0));
                p.addPotionEffect(inf(PotionEffectType.RESISTANCE, 0));
            }
            case ICE       -> p.addPotionEffect(inf(PotionEffectType.RESISTANCE, 0));
            case LIGHT     -> {
                p.addPotionEffect(inf(PotionEffectType.STRENGTH, 0));
                p.addPotionEffect(inf(PotionEffectType.REGENERATION, 0));
            }
            case DARKNESS  -> {
                p.addPotionEffect(inf(PotionEffectType.STRENGTH, 0));
                p.addPotionEffect(inf(PotionEffectType.NIGHT_VISION, 0));
            }
        }
    }

    private void clearPassives(Player p) {
        if (p == null) return;
        p.getActivePotionEffects().forEach(pe -> p.removePotionEffect(pe.getType()));
    }

    private PotionEffect inf(PotionEffectType type, int amp) {
        return new PotionEffect(type, Integer.MAX_VALUE, amp, false, false);
    }
}
