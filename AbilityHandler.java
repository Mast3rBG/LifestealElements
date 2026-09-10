package master.com.element;

import net.kyori.adventure.text.Component;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import java.util.HashSet;
import java.util.Set;

public class AbilityHandler {

    private final LifestealElement plugin;

    public AbilityHandler(LifestealElement plugin) { this.plugin = plugin; }


    public void activate1(Player p, Element el) {
        if (!WorldGuardHook.canUseAbility(p, p.getLocation())) {
            p.sendMessage("§cYou cannot use abilities in a protected region!");
            return;
        }
        switch (el) {
            case FLAME     -> flameFireball(p);
            case LIGHTNING -> lightningStrike(p);
            case WATER     -> waterTidalBurst(p);
            case EARTH     -> earthCrack(p);
            case ICE       -> iceIceberg(p);
            case LIGHT     -> lightDivineArrow(p);
            case DARKNESS  -> darknessDarkSpear(p);
        }
    }

    public void activate2(Player p, Element el) {
        if (!WorldGuardHook.canUseAbility(p, p.getLocation())) {
            p.sendMessage("§cYou cannot use abilities in a protected region!");
            return;
        }
        switch (el) {
            case FLAME     -> flameMeteorRain(p);
            case LIGHTNING -> lightningStormSurge(p);
            case WATER     -> waterVeil(p);
            case EARTH     -> earthGroundShift(p);
            case ICE       -> iceFrostField(p);
            case LIGHT     -> lightJudgment(p);
            case DARKNESS  -> darknessBlackHole(p);
        }
    }


    private void flameFireball(Player p) {
        actionBar(p, "§c§lFireball §8— §7Launched!");
        p.playSound(p.getLocation(), Sound.ENTITY_BLAZE_SHOOT, 1f, 0.8f);

        Fireball fb = p.launchProjectile(Fireball.class);
        fb.setIsIncendiary(true);
        fb.setYield(2.5f);
        fb.setDirection(p.getEyeLocation().getDirection().multiply(2.5));

        new BukkitRunnable() {
            int t = 0;
            public void run() {
                if (fb.isDead() || t++ > 60) { cancel(); return; }
                fb.getWorld().spawnParticle(Particle.FLAME, fb.getLocation(), 4, 0.1, 0.1, 0.1, 0.02);
                fb.getWorld().spawnParticle(Particle.SMOKE, fb.getLocation(), 2, 0.05, 0.05, 0.05, 0.01);
            }
        }.runTaskTimer(plugin, 0, 1);

        p.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 2400, 0, false, false));
        p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 1800, 0, false, false));
    }

    private void flameMeteorRain(Player p) {
        actionBar(p, "§c§lMeteor Rain §8— §7Raining down!");
        p.playSound(p.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 0.6f, 0.5f);
        Location above = p.getLocation().clone().add(0, 30, 0);

        new BukkitRunnable() {
            int wave = 0;
            public void run() {
                if (wave++ >= 4) { cancel(); return; }
                for (int i = 0; i < 5; i++) {
                    double ox = (Math.random() - 0.5) * 16;
                    double oz = (Math.random() - 0.5) * 16;
                    Location spawnLoc = above.clone().add(ox, 0, oz);
                    Fireball fb = p.getWorld().spawn(spawnLoc, Fireball.class,
                            CreatureSpawnEvent.SpawnReason.CUSTOM);
                    fb.setIsIncendiary(true);
                    fb.setYield(1.8f);
                    fb.setShooter(p);
                    fb.setDirection(new Vector(ox * 0.05, -2.5, oz * 0.05).normalize());
                }
                p.getWorld().spawnParticle(Particle.FLAME, p.getLocation().add(0,1,0), 30, 4, 0.5, 4, 0.1);
                p.playSound(p.getLocation(), Sound.ENTITY_BLAZE_SHOOT, 0.8f, 1.2f);
            }
        }.runTaskTimer(plugin, 0, 12);

        p.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 1800, 1, false, false));
        p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 2400, 0, false, false));
    }


    private void lightningStrike(Player p) {
        LivingEntity target = getTarget(p, 25);
        if (target == null) {
            actionBar(p, "§e§lThunder Strike §8— §cNo target!");
            p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1f, 0.5f);
            return;
        }
        if (!WorldGuardHook.canUseAbility(p, target.getLocation())) {
            p.sendMessage("§cYou cannot damage players here."); return;
        }
        actionBar(p, "§e§lThunder Strike §8— §7" + target.getName() + " struck!");
        p.playSound(p.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 1f, 1f);

        target.getWorld().strikeLightning(target.getLocation());
        target.damage(10.0, p);
        target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 60, 2, false, false));

        Vector dir = target.getLocation().toVector().subtract(p.getLocation().toVector());
        double dist = dir.length();
        dir.normalize();
        for (double d = 0; d < dist; d += 0.5) {
            Location pt = p.getLocation().clone().add(dir.clone().multiply(d)).add(0, 1, 0);
            pt.add((Math.random() - 0.5) * 0.4, (Math.random() - 0.5) * 0.4, (Math.random() - 0.5) * 0.4);
            p.getWorld().spawnParticle(Particle.ELECTRIC_SPARK, pt, 1, 0, 0, 0, 0);
        }
    }

    private void lightningStormSurge(Player p) {
        actionBar(p, "§e§lStorm Surge §8— §7Calling the storm!");
        p.playSound(p.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 1f, 0.6f);
        Location base = p.getLocation();

        new BukkitRunnable() {
            int waves = 0;
            public void run() {
                if (waves++ >= 5) { cancel(); return; }
                for (int i = 0; i < 6; i++) {
                    double ox = (Math.random() - 0.5) * 30;
                    double oz = (Math.random() - 0.5) * 30;
                    Location strike = base.clone().add(ox, 0, oz);
                    p.getWorld().strikeLightning(strike);
                }
                for (Entity ent : base.getWorld().getNearbyEntities(base, 15, 10, 15)) {
                    if (ent instanceof LivingEntity le && !le.equals(p)) {
                        if (!WorldGuardHook.canUseAbility(p, le.getLocation())) continue;
                        le.damage(4.0, p);
                        le.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 40, 1, false, false));
                    }
                }
                p.getWorld().spawnParticle(Particle.ELECTRIC_SPARK, base.clone().add(0,1,0), 60, 8, 2, 8, 0.3);
                p.playSound(base, Sound.ENTITY_LIGHTNING_BOLT_IMPACT, 0.7f, 1f);
            }
        }.runTaskTimer(plugin, 0, 20);

        p.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 2400, 1, false, false));
        p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 2400, 0, false, false));
    }


    private void waterTidalBurst(Player p) {
        actionBar(p, "§3§lTidal Burst §8— §7Wave released!");
        p.playSound(p.getLocation(), Sound.ENTITY_DOLPHIN_SPLASH, 1f, 0.7f);
        Location center = p.getLocation();

        new BukkitRunnable() {
            double radius = 0;
            public void run() {
                if (radius > 8) { cancel(); return; }
                for (double angle = 0; angle < Math.PI * 2; angle += Math.PI / 16) {
                    double x = Math.cos(angle) * radius;
                    double z = Math.sin(angle) * radius;
                    center.getWorld().spawnParticle(Particle.SPLASH, center.clone().add(x, 0.3, z), 1, 0, 0, 0, 0);
                    center.getWorld().spawnParticle(Particle.BUBBLE_POP, center.clone().add(x, 0.5, z), 1, 0, 0, 0, 0);
                }
                for (Entity ent : center.getWorld().getNearbyEntities(center, radius + 0.5, 2, radius + 0.5)) {
                    if (ent instanceof LivingEntity le && !le.equals(p)) {
                        if (!WorldGuardHook.canUseAbility(p, le.getLocation())) continue;
                        Vector kb = le.getLocation().toVector().subtract(center.toVector()).normalize().multiply(2.5).setY(0.6);
                        le.setVelocity(kb);
                        le.damage(4.0, p);
                    }
                }
                radius += 0.8;
            }
        }.runTaskTimer(plugin, 0, 2);

        p.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 1800, 0, false, false));
    }

    private void waterVeil(Player p) {
        actionBar(p, "§3§lWater Veil §8— §7Concealed!");
        p.playSound(p.getLocation(), Sound.BLOCK_WATER_AMBIENT, 1f, 1.5f);
        Location center = p.getLocation();
        Set<Block> changed = new HashSet<>();
        int radius = 4;

        for (int x = -radius; x <= radius; x++) {
            for (int y = 0; y <= 6; y++) {
                for (int z = -radius; z <= radius; z++) {
                    if (x * x + y * y + z * z <= radius * radius) {
                        Block b = center.clone().add(x, y, z).getBlock();
                        if (b.getType() == Material.AIR) {
                            b.setType(Material.WATER);
                            changed.add(b);
                        }
                    }
                }
            }
        }

        for (Entity ent : p.getNearbyEntities(10, 5, 10)) {
            if (ent instanceof Player enemy && !enemy.equals(p)) {
                enemy.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 100, 0, false, false));
                enemy.sendMessage("§3You are caught in a §bWater Veil§3!");
            }
        }

        p.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 100, 0, true, false));
        p.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 1800, 1, false, false));
        p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 1200, 0, false, false));

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            changed.forEach(b -> { if (b.getType() == Material.WATER) b.setType(Material.AIR); });
        }, 100L);
    }


    private void earthCrack(Player p) {
        actionBar(p, "§2§lEarth Crack §8— §7The ground trembles!");
        p.playSound(p.getLocation(), Sound.BLOCK_STONE_BREAK, 1f, 0.4f);
        p.playSound(p.getLocation(), Sound.ENTITY_IRON_GOLEM_HURT, 0.8f, 0.5f);
        Location center = p.getLocation();

        new BukkitRunnable() {
            int run = 0;
            public void run() {
                if (run++ >= 10) { cancel(); return; }
                double radius = run * 0.8;
                for (double angle = 0; angle < Math.PI * 2; angle += Math.PI / 20) {
                    double x = Math.cos(angle) * radius;
                    double z = Math.sin(angle) * radius;
                    center.getWorld().spawnParticle(Particle.BLOCK, center.clone().add(x, 0.1, z),
                            3, 0.2, 0.1, 0.2, 0, Material.DIRT.createBlockData());
                    center.getWorld().spawnParticle(Particle.BLOCK, center.clone().add(x, 0.1, z),
                            2, 0.2, 0.1, 0.2, 0, Material.STONE.createBlockData());
                }
                for (Entity ent : center.getWorld().getNearbyEntities(center, radius + 1, 3, radius + 1)) {
                    if (ent instanceof LivingEntity le && !le.equals(p)) {
                        if (!WorldGuardHook.canUseAbility(p, le.getLocation())) continue;
                        le.damage(5.0, p);
                        le.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 80, 3, false, false));
                    }
                }
                p.getWorld().playEffect(center.clone().add(0, 0.5, 0), Effect.STEP_SOUND, Material.STONE);
            }
        }.runTaskTimer(plugin, 0, 4);

        p.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 1800, 0, false, false));
        p.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 2400, 1, false, false));
    }

    private void earthGroundShift(Player p) {
        if (p.getLocation().getPitch() < -50) {
            actionBar(p, "§2§lGround Rise §8— §7Earth lifts!");
            p.playSound(p.getLocation(), Sound.BLOCK_STONE_PLACE, 1f, 0.5f);
            startEarthLift(p, 5, 6);
        } else if (p.getLocation().getPitch() > 50) {
            actionBar(p, "§2§lGround Destroy §8— §7Earth breaks!");
            p.playSound(p.getLocation(), Sound.BLOCK_GRAVEL_BREAK, 1f, 0.6f);
            startEarthDig(p, 5, 6);
        } else {
            actionBar(p, "§2§lGround Shift §8— §cLook up or down to activate!");
            p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1f, 0.5f);
            return;
        }
        p.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 2400, 1, false, false));
        p.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 1800, 3, false, false));
    }

    private void startEarthLift(Player p, int seconds, int radius) {
        Location center = p.getLocation();
        int runs = seconds * 4;
        new BukkitRunnable() {
            int count = 0;
            public void run() {
                if (count++ >= runs) { cancel(); return; }
                for (int dx = -radius; dx <= radius; dx++) {
                    for (int dz = -radius; dz <= radius; dz++) {
                        if (dx * dx + dz * dz > radius * radius) continue;
                        Block base = center.clone().add(dx, -1, dz).getBlock();
                        if (base.getType().isSolid() && base.getType() != Material.BEDROCK) {
                            Location dest = base.getLocation().add(0, 1, 0);
                            if (dest.getBlock().isEmpty()) {
                                dest.getBlock().setType(base.getType());
                                dest.getBlock().setBlockData(base.getBlockData());
                                base.setType(Material.AIR);
                                center.getWorld().spawnParticle(Particle.BLOCK, dest, 3, 0.3, 0.1, 0.3, 0, base.getBlockData());
                            }
                        }
                    }
                }
            }
        }.runTaskTimer(plugin, 0, 5);
    }

    private void startEarthDig(Player p, int seconds, int radius) {
        Location center = p.getLocation();
        int runs = seconds * 4;
        new BukkitRunnable() {
            int count = 0;
            public void run() {
                if (count++ >= runs) { cancel(); return; }
                for (int dx = -radius; dx <= radius; dx++) {
                    for (int dz = -radius; dz <= radius; dz++) {
                        for (int dy = 1; dy <= 6; dy++) {
                            if (dx * dx + dz * dz + dy * dy > radius * radius) continue;
                            Block b = center.clone().add(dx, -dy, dz).getBlock();
                            if (b.getType().isSolid() && b.getType() != Material.BEDROCK && Math.random() < 0.3) {
                                center.getWorld().spawnParticle(Particle.BLOCK, b.getLocation().add(0.5, 1, 0.5),
                                        4, 0.3, 0.1, 0.3, 0, b.getBlockData());
                                b.breakNaturally();
                            }
                        }
                    }
                }
            }
        }.runTaskTimer(plugin, 0, 5);
    }


    private void iceIceberg(Player p) {
        actionBar(p, "§b§lIceberg §8— §7Snowball launched!");
        p.playSound(p.getLocation(), Sound.ENTITY_SNOWBALL_THROW, 1f, 0.8f);
        p.playSound(p.getLocation(), Sound.BLOCK_GLASS_BREAK, 0.5f, 1.5f);

        Snowball sb = p.launchProjectile(Snowball.class);
        sb.setShooter(p);
        sb.setVelocity(p.getEyeLocation().getDirection().multiply(3));

        new BukkitRunnable() {
            public void run() {
                if (sb.isDead() || sb.isOnGround()) {
                    Location loc = sb.getLocation();
                    if (!WorldGuardHook.canUseAbility(p, loc)) { cancel(); return; }
                    createIceSphere(loc, 4, p);
                    loc.getWorld().spawnParticle(Particle.SNOWFLAKE, loc, 80, 3, 1, 3, 0.2);
                    loc.getWorld().spawnParticle(Particle.ITEM_SNOWBALL, loc, 40, 2, 1, 2, 0.3);
                    loc.getWorld().playSound(loc, Sound.BLOCK_GLASS_BREAK, 1f, 0.5f);
                    cancel();
                } else {
                    sb.getWorld().spawnParticle(Particle.SNOWFLAKE, sb.getLocation(), 3, 0.1, 0.1, 0.1, 0.05);
                }
            }
        }.runTaskTimer(plugin, 0, 2);

        p.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 1800, 1, false, false));
    }

    private void iceFrostField(Player p) {
        actionBar(p, "§b§lFrost Field §8— §7Ground frozen!");
        p.playSound(p.getLocation(), Sound.BLOCK_GLASS_BREAK, 1f, 0.6f);
        Location center = p.getLocation();
        Set<Block> changed = new HashSet<>();
        int radius = 7;

        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                if (x * x + z * z > radius * radius) continue;
                Block b = center.clone().add(x, -1, z).getBlock();
                if (b.getType().isAir() || b.isPassable()) {
                    b.setType(Math.random() < 0.6 ? Material.ICE : Material.PACKED_ICE);
                    changed.add(b);
                }
            }
        }
        center.getWorld().spawnParticle(Particle.SNOWFLAKE, center.clone().add(0, 1, 0), 120, radius, 0.5, radius, 0.1);

        new BukkitRunnable() {
            int t = 0;
            public void run() {
                for (Entity ent : center.getWorld().getNearbyEntities(center, radius, 3, radius)) {
                    if (ent instanceof LivingEntity le && !le.equals(p)) {
                        if (!WorldGuardHook.canUseAbility(p, le.getLocation())) continue;
                        le.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 40, 10, true, false));
                        le.damage(2.0, p);
                        le.getWorld().spawnParticle(Particle.SNOWFLAKE, le.getLocation().add(0,1,0), 5, 0.3, 0.3, 0.3, 0);
                    }
                }
                t += 20;
                if (t >= 120) {
                    changed.forEach(b -> { if (b.getType() == Material.ICE || b.getType() == Material.PACKED_ICE) b.setType(Material.AIR); });
                    cancel();
                }
            }
        }.runTaskTimer(plugin, 0, 20);
    }

    private void createIceSphere(Location center, int radius, Player owner) {
        Set<Block> changed = new HashSet<>();
        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    if (x*x + y*y + z*z <= radius*radius) {
                        Block b = center.clone().add(x, y, z).getBlock();
                        if (b.getType() == Material.WATER || b.getType() == Material.AIR) {
                            b.setType(Math.random() < 0.5 ? Material.ICE : Material.PACKED_ICE);
                            changed.add(b);
                        }
                    }
                }
            }
        }
        for (Entity ent : owner.getNearbyEntities(radius + 1, radius + 1, radius + 1)) {
            if (ent instanceof LivingEntity le && !le.equals(owner)) {
                if (!WorldGuardHook.canUseAbility(owner, le.getLocation())) continue;
                le.damage(4.0, owner);
                le.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 80, 4, false, false));
            }
        }
        Bukkit.getScheduler().runTaskLater(plugin, () ->
                changed.forEach(b -> { if (b.getType() == Material.ICE || b.getType() == Material.PACKED_ICE) b.setType(Material.AIR); }),
                20L * 12);
    }


    private void lightDivineArrow(Player p) {
        actionBar(p, "§f§lDivine Arrow §8— §7Fired!");
        p.playSound(p.getLocation(), Sound.ENTITY_ARROW_SHOOT, 1f, 1.5f);
        p.playSound(p.getLocation(), Sound.BLOCK_BEACON_POWER_SELECT, 0.5f, 2f);

        Arrow arrow = p.launchProjectile(Arrow.class);
        arrow.setDamage(10.0);
        arrow.setCritical(true);
        arrow.setGlowing(true);
        arrow.setVelocity(p.getEyeLocation().getDirection().multiply(4));
        arrow.addCustomEffect(new PotionEffect(PotionEffectType.GLOWING, 100, 0), true);
        arrow.addScoreboardTag("divine_arrow");
        arrow.setShooter(p);

        new BukkitRunnable() {
            public void run() {
                if (arrow.isDead() || arrow.isOnGround()) { cancel(); return; }
                arrow.getWorld().spawnParticle(Particle.END_ROD, arrow.getLocation(), 3, 0.05, 0.05, 0.05, 0.02);
                arrow.getWorld().spawnParticle(Particle.GLOW, arrow.getLocation(), 2, 0.05, 0.05, 0.05, 0);
            }
        }.runTaskTimer(plugin, 0, 1);

        p.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 2400, 1, false, false));
        p.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 1800, 0, false, false));
    }

    private void lightJudgment(Player p) {
        actionBar(p, "§f§lDivine Judgment §8— §7Three waves incoming!");
        p.playSound(p.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 1f, 1.5f);
        Location center = p.getLocation();

        for (int wave = 0; wave < 3; wave++) {
            int delay = wave * 25;
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                center.getWorld().strikeLightningEffect(center);
                center.getWorld().spawnParticle(Particle.END_ROD, center.clone().add(0, 1, 0), 60, 5, 0.5, 5, 0.15);
                center.getWorld().spawnParticle(Particle.GLOW, center.clone().add(0, 1, 0), 40, 5, 0.5, 5, 0.1);
                center.getWorld().playSound(center, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 0.8f, 1.5f);
                for (Entity ent : center.getWorld().getNearbyEntities(center, 20, 8, 20)) {
                    if (ent instanceof LivingEntity le && !le.equals(p)) {
                        if (!WorldGuardHook.canUseAbility(p, le.getLocation())) continue;
                        le.damage(6.0, p);
                        le.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 80, 0, false, false));
                    }
                }
            }, delay);
        }

        p.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 2400, 3, false, false));
        p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 1800, 0, false, false));
    }


    private void darknessDarkSpear(Player p) {
        actionBar(p, "§8§lDark Spear §8— §7Summoned! (30s)");
        p.playSound(p.getLocation(), Sound.ENTITY_WITHER_SPAWN, 0.4f, 1.8f);
        p.playSound(p.getLocation(), Sound.BLOCK_PORTAL_TRIGGER, 0.3f, 0.5f);

        p.getWorld().spawnParticle(Particle.PORTAL, p.getLocation().add(0, 1, 0), 60, 0.5, 0.5, 0.5, 0.5);
        p.getWorld().spawnParticle(Particle.SMOKE, p.getLocation().add(0, 1, 0), 30, 0.3, 0.3, 0.3, 0.05);

        ItemStack spear = new ItemStack(Material.NETHERITE_SWORD);
        ItemMeta meta = spear.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§8§lDark Spear");
            meta.setLore(java.util.List.of("§7Vanishes in §c30s", "§8Summoned by Darkness"));
            org.bukkit.NamespacedKey tag = new org.bukkit.NamespacedKey(plugin, "dark_spear");
            meta.getPersistentDataContainer().set(tag, org.bukkit.persistence.PersistentDataType.BOOLEAN, true);
            spear.setItemMeta(meta);
        }
        spear.addUnsafeEnchantment(org.bukkit.enchantments.Enchantment.SHARPNESS, 8);
        spear.addUnsafeEnchantment(org.bukkit.enchantments.Enchantment.UNBREAKING, 10);
        spear.addUnsafeEnchantment(org.bukkit.enchantments.Enchantment.FIRE_ASPECT, 3);

        p.getInventory().addItem(spear);
        p.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 2400, 1, false, false));
        p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 1800, 0, false, false));

        final ItemStack spearRef = spear;
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            p.getInventory().remove(spearRef);
            p.sendActionBar(Component.text("§8§lDark Spear §8— §7Dissolved into shadows."));
            p.playSound(p.getLocation(), Sound.BLOCK_PORTAL_TRIGGER, 0.3f, 1.8f);
            p.getWorld().spawnParticle(Particle.PORTAL, p.getLocation().add(0,1,0), 30, 0.5, 0.5, 0.5, 0.3);
        }, 20L * 30);
    }

    private void darknessBlackHole(Player p) {
        actionBar(p, "§8§lBlack Hole §8— §7Unleashed!");
        p.playSound(p.getLocation(), Sound.ENTITY_WITHER_DEATH, 0.5f, 0.3f);
        p.playSound(p.getLocation(), Sound.BLOCK_PORTAL_AMBIENT, 1f, 0.3f);

        Location center = p.getLocation().clone().add(0, 1, 0);
        Location groundCenter = p.getLocation().clone();

        new BukkitRunnable() {
            int run = 0;
            public void run() {
                if (run++ >= 24) {
                    p.sendActionBar(Component.text("§8§lBlack Hole §8— §7Collapsed."));
                    cancel();
                    return;
                }
                double r = 3.5;
                double offset = run * 0.3;
                for (double theta = 0; theta < Math.PI * 2; theta += Math.PI / 10) {
                    for (double phi = 0; phi < Math.PI; phi += Math.PI / 10) {
                        double x = r * Math.cos(theta + offset) * Math.sin(phi);
                        double y = r * Math.cos(phi);
                        double z = r * Math.sin(theta + offset) * Math.sin(phi);
                        center.getWorld().spawnParticle(Particle.PORTAL, center.clone().add(x, y, z), 1, 0, 0, 0, 0);
                    }
                }
                center.getWorld().spawnParticle(Particle.SMOKE, center, 10, 0.5, 0.5, 0.5, 0.02);

                for (Entity ent : p.getNearbyEntities(20, 20, 20)) {
                    if (ent instanceof LivingEntity le && !le.equals(p)) {
                        if (!WorldGuardHook.canUseAbility(p, le.getLocation())) continue;
                        Vector pull = center.toVector().subtract(le.getLocation().toVector()).normalize().multiply(0.8);
                        le.setVelocity(le.getVelocity().add(pull));
                        if (run % 5 == 0) le.damage(6.0, p);
                    }
                }
                p.getWorld().playSound(center, Sound.BLOCK_PORTAL_AMBIENT, 0.3f, 0.5f);
            }
        }.runTaskTimer(plugin, 0, 5);

        p.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, 3600, 0, false, false));
        p.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 2400, 1, false, false));
    }


    private LivingEntity getTarget(Player p, double range) {
        RayTraceResult result = p.getWorld().rayTraceEntities(
                p.getEyeLocation(),
                p.getEyeLocation().getDirection(),
                range,
                0.3,
                e -> e instanceof LivingEntity && !e.equals(p)
        );
        if (result != null && result.getHitEntity() instanceof LivingEntity le) return le;
        return null;
    }

    private void actionBar(Player p, String msg) {
        p.sendActionBar(Component.text(msg));
    }
}
