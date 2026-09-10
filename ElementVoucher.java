package master.com.element;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;

public class ElementVoucher implements Listener {

    private final LifestealElement plugin;

    public ElementVoucher(LifestealElement plugin) { this.plugin = plugin; }

    public static ItemStack createVoucher(Element el) {
        ItemStack paper = new ItemStack(Material.PAPER);
        ItemMeta meta = paper.getItemMeta();
        if (meta == null) return paper;

        meta.setDisplayName(el.getColor() + "§l" + el.getDisplay() + " Element Voucher");
        meta.setLore(List.of(
            "§8§m──────────────────",
            "§7Right-click to claim the",
            el.getColor() + "§l" + el.getDisplay() + " §7element.",
            "",
            "§8Single use — cannot be dropped.",
            "§8§m──────────────────"
        ));
        NamespacedKey key = new NamespacedKey(LifestealElement.getInstance(), "voucher_element");
        meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, el.name());
        paper.setItemMeta(meta);
        return paper;
    }

    @EventHandler
    public void onUseVoucher(PlayerInteractEvent e) {
        if (e.getHand() != EquipmentSlot.HAND) return;
        if (e.getAction() != Action.RIGHT_CLICK_AIR && e.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        Player p = e.getPlayer();
        ItemStack item = e.getItem();
        if (item == null || !item.hasItemMeta()) return;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        NamespacedKey key = new NamespacedKey(plugin, "voucher_element");
        if (!meta.getPersistentDataContainer().has(key, PersistentDataType.STRING)) return;
        e.setCancelled(true);

        String id = meta.getPersistentDataContainer().get(key, PersistentDataType.STRING);
        Element el;
        try { el = Element.valueOf(id); }
        catch (Exception ex) { p.sendMessage("§cInvalid voucher!"); return; }

        Element current = ElementManager.getElement(p);
        if (current == el) {
            p.sendMessage("§c§lALREADY §8» §7You already have the " + el.getColor() + "§l" + el.getDisplay() + " §7element!");
            p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1f, 0.5f);
            return;
        }

        for (int i = 0; i < p.getInventory().getSize(); i++) {
            ItemStack it = p.getInventory().getItem(i);
            if (it != null && Element.fromItem(it) != null) p.getInventory().setItem(i, null);
        }

        ElementManager.setElement(p, el);
        p.getInventory().addItem(el.buildItem(plugin));

        item.setAmount(Math.max(0, item.getAmount() - 1));

        p.sendMessage("");
        p.sendMessage("§8§m────────────────────────");
        p.sendMessage(el.getColor() + "§l  ELEMENT CLAIMED");
        p.sendMessage("§7  You have claimed the " + el.getColor() + "§l" + el.getDisplay() + " §7element!");
        p.sendMessage("§7  Hold your element item to activate your passives.");
        p.sendMessage("§8§m────────────────────────");
        p.sendMessage("");
        p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1.2f);
        p.playSound(p.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 0.5f, 1.5f);
    }
}
