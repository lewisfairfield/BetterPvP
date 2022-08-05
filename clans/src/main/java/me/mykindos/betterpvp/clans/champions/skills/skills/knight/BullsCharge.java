package me.mykindos.betterpvp.clans.champions.skills.skills.knight;

import me.mykindos.betterpvp.clans.Clans;
import me.mykindos.betterpvp.clans.champions.ChampionsManager;
import me.mykindos.betterpvp.clans.champions.roles.Role;
import me.mykindos.betterpvp.clans.champions.skills.Skill;
import me.mykindos.betterpvp.clans.champions.skills.config.SkillConfigFactory;
import me.mykindos.betterpvp.clans.champions.skills.data.SkillActions;
import me.mykindos.betterpvp.clans.champions.skills.data.SkillType;
import me.mykindos.betterpvp.clans.champions.skills.types.CooldownSkill;
import me.mykindos.betterpvp.clans.champions.skills.types.InteractSkill;
import me.mykindos.betterpvp.core.combat.events.CustomDamageEvent;
import me.mykindos.betterpvp.core.framework.updater.UpdateEvent;
import me.mykindos.betterpvp.core.listener.BPvPListener;
import me.mykindos.betterpvp.core.utilities.UtilFormat;
import me.mykindos.betterpvp.core.utilities.UtilMessage;
import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.HashMap;

@Singleton
@BPvPListener
public class BullsCharge extends Skill implements Listener, InteractSkill, CooldownSkill {

    private final HashMap<String, Long> running = new HashMap<>();

    @Inject
    public BullsCharge(Clans clans, ChampionsManager championsManager, SkillConfigFactory configFactory) {
        super(clans, championsManager, configFactory);
    }

    @Override
    public String getName() {
        return "Bulls Charge";
    }

    @Override
    public String[] getDescription(int level) {
        return new String[]{
                "Right click with an Axe to activate.",
                "",
                "Enter a rage, gaining massive movement speed",
                "and slowing anything you hit for 3 seconds",
                "",
                "While charging, you take no knockback.",
                "",
                "Cooldown: " + ChatColor.GREEN + getCooldown(level)
        };
    }

    @Override
    public void activate(Player player, int level) {
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 60, 2));
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_SCREAM, 1.5F, 0.0F);
        player.getWorld().playEffect(player.getLocation(), Effect.STEP_SOUND, 49);
        running.put(player.getName(), System.currentTimeMillis() + 4000L);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onDamage(CustomDamageEvent event) {
        if (event.isCancelled()) return;

        if (event.getDamagee() instanceof Player player) {
            if (running.containsKey(player.getName())) {
                event.setKnockback(false);
            }
        }

        if (event.getCause() != EntityDamageEvent.DamageCause.ENTITY_ATTACK) {
            return;
        }

        if (event.getDamager() instanceof Player damager) {

            final LivingEntity damagee = (LivingEntity) event.getDamagee();

            if (running.containsKey(damager.getName())) {
                if (System.currentTimeMillis() >= running.get(damager.getName())) {
                    running.remove(damager.getName());
                    return;
                }

                event.setKnockback(false);

                damagee.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 60, 2));
                damager.removePotionEffect(PotionEffectType.SPEED);

                damager.getWorld().playSound(damager.getLocation(), Sound.ENTITY_ENDERMAN_SCREAM, 1.5F, 0.0F);
                damager.getWorld().playSound(damager.getLocation(), Sound.ENTITY_ZOMBIE_ATTACK_IRON_DOOR, 1.5F, 0.5F);

                if (event.getDamagee() instanceof Player damaged) {
                    UtilMessage.message(damaged, getClassType().getName(), ChatColor.YELLOW + damager.getName() + ChatColor.GRAY + " hit you with " + ChatColor.GREEN + getName() + ChatColor.GRAY + ".");
                    UtilMessage.message(damager, getClassType().getName(), "You hit " + ChatColor.YELLOW + damaged.getName() + ChatColor.GRAY + " with " + ChatColor.GREEN + getName() + ChatColor.GRAY + ".");
                    running.remove(damager.getName());
                    return;
                }

                UtilMessage.message(damager, getClassType().getName(), "You hit a " + ChatColor.YELLOW + UtilFormat.cleanString(damagee.getType().toString())
                        + ChatColor.GRAY + " with " + ChatColor.GREEN + getName() + ChatColor.GRAY + ".");
                running.remove(damager.getName());
            }
        }
    }

    @UpdateEvent(delay = 1000)
    public void onUpdate() {
        running.entrySet().removeIf(entry -> entry.getValue() - System.currentTimeMillis() <= 0);
    }

    @Override
    public Action[] getActions() {
        return SkillActions.RIGHT_CLICK;
    }

    @Override
    public double getCooldown(int level) {
        return getSkillConfig().getCooldown() - (level - 1);
    }

    @Override
    public Role getClassType() {
        return Role.KNIGHT;
    }

    @Override
    public SkillType getType() {
        return SkillType.AXE;
    }

}