package master.com.element;

import net.kyori.adventure.text.Component;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;

public enum Element {

    FLAME    (Material.BLAZE_POWDER,      1001, "§c", "Flame",
              "§7Fire resistance",
              "§c▸ §fFireball §8— §7Launch a homing fireball",
              "§c▸ §fMeteor Rain §8— §7Burst of fireballs overhead"),

    LIGHTNING(Material.LIGHTNING_ROD,     1002, "§e", "Lightning",
              "§7Speed II",
              "§e▸ §fThunder Strike §8— §7Strike a target with lightning",
              "§e▸ §fStorm Surge §8— §7Thunderstorm in a wide radius"),

    WATER    (Material.PRISMARINE_SHARD,  1003, "§3", "Water",
              "§7Water breathing + Dolphin's Grace",
              "§3▸ §fTidal Burst §8— §7Knockback wave around you",
              "§3▸ §fWater Veil §8— §7Invisibility + blind nearby enemies"),

    EARTH    (Material.MOSS_BLOCK,        1004, "§2", "Earth",
              "§7Haste + Resistance I",
              "§2▸ §fEarth Crack §8— §7AOE tremor — damage + slowness",
              "§2▸ §fGround Shift §8— §7Look up: rise | Look down: dig"),

    ICE      (Material.PACKED_ICE,        1005, "§b", "Ice",
              "§7Resistance I",
              "§b▸ §fIceberg §8— §7Launch freezing snowball projectile",
              "§b▸ §fFrost Field §8— §7Ice floor freezes enemies around you"),

    LIGHT    (Material.GLOWSTONE_DUST,    1006, "§f", "Light",
              "§7Strength I + Regeneration I",
              "§f▸ §fDivine Arrow §8— §7High-damage glowing arrow",
              "§f▸ §fJudgment §8— §7Triple AOE lightning wave"),

    DARKNESS (Material.COAL,              1007, "§8", "Darkness",
              "§7Strength I + Night Vision",
              "§8▸ §fDark Spear §8— §7Summon a temporary netherite spear",
              "§8▸ §fBlack Hole §8— §7Pull + damage all nearby enemies");

    private final Material  material;
    private final int       customModelData;
    private final String    color;
    private final String    display;
    private final String    passiveDesc;
    private final String    ability1Desc;
    private final String    ability2Desc;

    Element(Material material, int cmd, String color, String display,
            String passiveDesc, String ability1Desc, String ability2Desc) {
        this.material        = material;
        this.customModelData = cmd;
        this.color           = color;
        this.display         = display;
        this.passiveDesc     = passiveDesc;
        this.ability1Desc    = ability1Desc;
        this.ability2Desc    = ability2Desc;
    }

    public Material getMaterial()    { return material; }
    public int      getCustomModelData() { return customModelData; }
    public String   getColor()       { return color; }
    public String   getDisplay()     { return display; }

    public ItemStack buildItem(LifestealElement plugin) {
        ItemStack item = new ItemStack(material);
        ItemMeta  meta = item.getItemMeta();
        if (meta == null) return item;

        meta.displayName(Component.text(color + "§l" + display));
        meta.setCustomModelData(customModelData);
        meta.setLore(List.of(
            "§8§m──────────────────",
            passiveDesc,
            "",
            "§6Passive",
            passiveDesc,
            "",
            "§6Abilities",
            ability1Desc,
            ability2Desc,
            "§8§m──────────────────",
            "§7Hold this item to activate your element."
        ));
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ATTRIBUTES,
                          ItemFlag.HIDE_ADDITIONAL_TOOLTIP);

        NamespacedKey key = new NamespacedKey(plugin, "element_id");
        meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, this.name());

        item.setItemMeta(meta);
        return item;
    }

    public ItemStack buildItem() { return buildItem(LifestealElement.getInstance()); }

    public static Element fromItem(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return null;
        ItemMeta meta = item.getItemMeta();
        NamespacedKey key = new NamespacedKey(LifestealElement.getInstance(), "element_id");
        if (meta.getPersistentDataContainer().has(key, PersistentDataType.STRING)) {
            String id = meta.getPersistentDataContainer().get(key, PersistentDataType.STRING);
            try { return Element.valueOf(id); } catch (Exception ignored) {}
        }
        if (meta.hasDisplayName()) {
            String stripped = ChatColor.stripColor(meta.getDisplayName()).trim();
            return fromDisplay(stripped);
        }
        return null;
    }

    public static Element fromDisplay(String name) {
        if (name == null) return null;
        String clean = ChatColor.stripColor(name).trim();
        for (Element e : values()) if (e.display.equalsIgnoreCase(clean)) return e;
        return null;
    }

    public static String getAllNames() {
        StringBuilder sb = new StringBuilder();
        for (Element e : values()) sb.append(e.color).append(e.display).append("§7, ");
        return sb.length() > 2 ? sb.substring(0, sb.length() - 2) : "";
    }
}
