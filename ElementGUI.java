package master.com.element;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class ElementGUI {

    private static final Material BORDER     = Material.GRAY_STAINED_GLASS_PANE;
    private static final Material CYAN_PANE  = Material.CYAN_STAINED_GLASS_PANE;
    private static final Material BLACK_PANE = Material.BLACK_STAINED_GLASS_PANE;

    public void openFor(Player staff) {
        Inventory inv = Bukkit.createInventory(null, 54, "§8§lElement Manager");

        for (int i = 0; i <= 8; i++) inv.setItem(i, i % 2 == 0 ? pane(CYAN_PANE) : pane(BORDER));
        inv.setItem(4, named(Material.NETHER_STAR, "§b§lElement Manager",
                List.of("§7Assign an element to a player.", "§7Players receive it via §6vouchers§7.",
                        "§8§m----------------------------",
                        "§eLeft-click §7an element to get its voucher.",
                        "§6Right-click §7for more options.")));

        for (int i : new int[]{9,18,27,36}) inv.setItem(i, pane(BORDER));
        for (int i : new int[]{17,26,35,44}) inv.setItem(i, pane(BORDER));

        int[] slots = {10,11,12,13,14,15,16,19,20,21,22,23,24,25,28,29,30,31,32,33,34,37,38,39,40,41,42,43};
        for (int s : slots) inv.setItem(s, pane(BORDER));

        int[] elementSlots = {11, 13, 15, 20, 22, 24, 31};
        Element[] elements = Element.values();
        for (int i = 0; i < elements.length && i < elementSlots.length; i++) {
            Element el = elements[i];
            ItemStack item = el.buildItem(LifestealElement.getInstance());
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                List<String> lore = meta.hasLore() ? new ArrayList<>(meta.getLore()) : new ArrayList<>();
                lore.add("");
                lore.add("§eLeft-Click §7to give a voucher to a player");
                lore.add("§7Use §b/elements give <player> " + el.getDisplay());
                meta.setLore(lore);
                item.setItemMeta(meta);
            }
            inv.setItem(elementSlots[i], item);
        }

        for (int i = 45; i <= 53; i++) inv.setItem(i, pane(BLACK_PANE));
        inv.setItem(49, named(Material.WRITABLE_BOOK, "§e§lQuick Commands",
                List.of("§b/elements give §f<player> <element>",
                        "§b/elements remove §f<player>",
                        "§b/elements voucher §f<element>",
                        "§b/elements list")));
        inv.setItem(53, named(Material.BARRIER, "§c§lClose", List.of("§7Click to close")));

        staff.openInventory(inv);
    }

    public void handleClick(InventoryClickEvent e) {
        if (!e.getView().getTitle().equals("§8§lElement Manager")) return;
        e.setCancelled(true);
        if (!(e.getWhoClicked() instanceof Player p)) return;
        ItemStack clicked = e.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;
        if (clicked.getType() == Material.BARRIER) { p.closeInventory(); return; }

        Element el = Element.fromItem(clicked);
        if (el == null) return;

        ItemStack voucher = ElementVoucher.createVoucher(el);
        p.getInventory().addItem(voucher);
        p.sendMessage("§a§lVOUCHER §8» §7Given §" + el.getColor() + el.getDisplay() + " §7voucher.");
        p.playSound(p.getLocation(), Sound.ENTITY_ITEM_PICKUP, 1f, 1.2f);
    }

    private ItemStack pane(Material mat) {
        ItemStack i = new ItemStack(mat);
        ItemMeta m = i.getItemMeta();
        if (m != null) { m.setDisplayName("§r"); i.setItemMeta(m); }
        return i;
    }

    private ItemStack named(Material mat, String name, List<String> lore) {
        ItemStack i = new ItemStack(mat);
        ItemMeta m = i.getItemMeta();
        if (m != null) { m.setDisplayName(name); m.setLore(lore); i.setItemMeta(m); }
        return i;
    }
}
