package labs.madskwerl.WeaponsOfPower;

import org.bukkit.entity.*;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import java.util.UUID;

public class WOP_EXPLOSION
{
    //called from EntityDamageByEntityEvent if Entity.getType() is EntityType.FIREBALL
    public static void onHit(EntityDamageByEntityEvent e)
    {
        System.out.println("WOP_EXPLOSION.onHit() Called");
        //locate attacker from source
        Entity source = e.getDamager();
        String sourceCustomName = source.getCustomName();
        if(sourceCustomName == null)
            sourceCustomName = "";

        Entity attacker = null;
        WeaponOfPowerData attackerWopData = WeaponOfPowerInventoryBank.WorldBank.getWeaponOfPowerData(sourceCustomName);
        boolean attackerHasWop = attackerWopData != null;

        if(attackerHasWop)
            attacker = attackerWopData.getPlayer();

        //locate defender
        Entity defender = e.getEntity();
        String defenderCustomName = defender.getCustomName();
        if(defenderCustomName == null)
            defenderCustomName = "";

        WeaponOfPowerData defenderWopData = WeaponOfPowerInventoryBank.WorldBank.getWeaponOfPowerData(defenderCustomName);
        boolean defenderHasWop = defenderWopData != null;

        //================================= Cancellation Block ======================================\
        //Note: additional cancel condition below that is not in cancellation block
        boolean bothPlayers = (attacker instanceof Player && defender instanceof  Player);
        boolean attackSelf = attacker == defender;
        boolean bothNotPlayers = !(attacker instanceof Player) && !(defender instanceof Player);
        boolean oneIsWOP = attackerWopData != null || defenderWopData != null;
        if ((bothPlayers && !attackSelf) || (bothNotPlayers && oneIsWOP))
        {
            e.setCancelled(true);
            return;
        }

        //================================= Damage Application Block ======================================
        int attackerLevel;
        int defenderLevel;
        if (attacker instanceof Player)
        {
            LivingEntityData attackerLED = LivingEntityBank.getLivingEntityData(attacker.getUniqueId());
            attackerLevel = attackerLED.getLevel();
            if (defenderHasWop)
                defenderLevel = (int)WeaponsOfPower.MONSTER_LEVEL_AVERAGE;
            else
                defenderLevel = LivingEntityBank.getLivingEntityData(attacker.getUniqueId()).getLevel();
        }
        else
        {
            defenderLevel = LivingEntityBank.getLivingEntityData(defender.getUniqueId()).getLevel();
            if (attackerHasWop)
                attackerLevel = (int)WeaponsOfPower.MONSTER_LEVEL_AVERAGE;
            else
                attackerLevel = LivingEntityBank.getLivingEntityData(defender.getUniqueId()).getLevel();
        }

        double levelRatioModifier = 1 + (attackerLevel - defenderLevel) * 0.01;

        int protectionLevel = !defenderHasWop ? 0 : defenderWopData.getProtectionLevel();
        double protectionModifier = 1 - protectionLevel * .1;
        if (e.getCause().equals(EntityDamageEvent.DamageCause.ENTITY_EXPLOSION))
        {
            int explosiveProtectionLevel = !defenderHasWop ? 0 : defenderWopData.getExplosiveProtectionLevel();
            double explosiveProtection = 1 - explosiveProtectionLevel * 0.15;
            if ((explosiveProtectionLevel > 0 && protectionLevel < 0) || (explosiveProtectionLevel < 0 && protectionLevel > 0))
                protectionModifier += explosiveProtection;
            else
                protectionModifier = (protectionLevel > explosiveProtectionLevel) ? protectionLevel:explosiveProtection;
        }

        int damageLevel = !attackerHasWop ? 0 : attackerWopData.getDamageLevel();
        double damageIncreaseModifier = 1 + damageLevel * 0.1;

        int wopBaseDamage = 0;
        if (attackerHasWop && defenderHasWop)
        {
            int attackerBase = LivingEntityBank.getLivingEntityData(attacker.getUniqueId()).getBaseATK();
            int defenderBase = LivingEntityBank.getLivingEntityData(defender.getUniqueId()).getBaseDEF();
            wopBaseDamage = attackerBase - defenderBase;
        }

        double damage = wopBaseDamage + 10 * levelRatioModifier * protectionModifier * damageIncreaseModifier; //Note: overriding event damage ("+ 10") to ensuring a more consistent damage
        e.setDamage(damage);
        System.out.println( "The Thing dealt " + damage + " damage to " + defender.getName());
    }
}
