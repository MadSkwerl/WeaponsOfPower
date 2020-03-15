package labs.madskwerl.WeaponsOfPower;

import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.util.Vector;


public class WOP_IRON_SWORD
{
    public static void onUse(PlayerInteractEvent e)
    {
        Player player = e.getPlayer();
        String customName = player.getCustomName();
        Block blockClicked = e.getClickedBlock();
        // If the player left clicks
        WeaponOfPowerData wopData = WeaponOfPowerInventoryBank.WorldBank.getWeaponOfPowerData(customName);

        //============================== Interact: Cool-Down/Durability =======================================
        long currentTime = System.currentTimeMillis();
        LivingEntityData livingEntityData = LivingEntityBank.getLivingEntityData(player.getUniqueId());
        long swingTimer = currentTime - livingEntityData.getLastAttackTime();
        int swingDelay = livingEntityData.getAttackDelay();
        if (swingTimer > swingDelay)            //if player's cool-down is finished //(currentTime - attackerLED.getLastWOPRegenTime()) > 100)                                    //and 100ms since last WOP regen (to prevent making too many)                                    //and 100ms since last WOP regen (to prevent making too many)
        {
            livingEntityData.setLastAttackTime(currentTime);                                           //then reset player cool-down period
            //handle infinity powerUp
            if(wopData.getInfiniteLevel() < 1)
            {
                int fragilityLevel = wopData.getSplashLevel(); //negative splash level is used as fragility
                int additionalAmmo = fragilityLevel < 0 ? fragilityLevel * -1 : 0;
                int currentAmmo = wopData.getAmmo();
                if(!wopData.removeAmmo(1 + additionalAmmo))//if ammo is too low
                {
                    e.setCancelled(true);
                    return;
                } else //otherwise, remove durability (ammo)
                {
                    System.out.println("Ammo: " + currentAmmo + " -> " + wopData.getAmmo());
                    WeaponsOfPower.NSA.refreshChargesArtifact(player);
                    //===================================== Interact: Regen ===========================================
                    //only start a new regen ammo recursion if current ammo is max and item has appropriate power
                    if (currentAmmo == wopData.getMaxAmmo() && wopData.getAmmoRegenLevel() != 0)
                    {
                        System.out.println("Regen Timer Started From Player Interact.");
                        new Regen_Ammo(wopData, livingEntityData).runTaskLater(WeaponsOfPower.PLUGIN, 20);
                    }
                    //======= Regen ====
                }
            }


            //==================================== Interact: Jamming ==========================================
            //roll for power, will either be jamming or volatile, not both
            int roll = WeaponsOfPower.RANDOM.nextInt(5);
            //Handle Jamming power
            if (wopData.getInfiniteLevel() * -1 > roll) //Negative infinite level used as jamming level
            {
                System.out.println("Interact Canceled: Jamming Power Triggered");
                e.setCancelled(true);
                return;
            }
            //======== End Jamming ====

            if (blockClicked == null)
                return;

            //================================= Interact: Volatile/Boom =======================================
            Location location = null;
            int boomLevel = wopData.getExplosiveLevel();
            if (boomLevel * -1 > roll) // boomLevel * -1 inverts the neg to pos
                location = player.getLocation(); //explode on player
            else if (boomLevel > roll)
                location = blockClicked.getLocation(); //explode where the player is looking

            if (location != null)
            {
                Fireball fireball = (Fireball) player.getWorld().spawnEntity(location, EntityType.FIREBALL); //fireball had the more control and aesthetics than creeper or tnt. Could not use world.createExplosion(), needed way to track entity
                fireball.setCustomName(String.valueOf(wopData.getHashCode())); //provides way to track entity
                fireball.setYield(2);
                fireball.setIsIncendiary(false);
                fireball.setVelocity(new Vector(0, -1000, 0)); //sends straight down fast enough to explode immediately
            }
            //======= End Volatile/Boom ====
        }
        //========= End Cool-Down =======
    }

    public static void onHit(EntityDamageByEntityEvent e)
    {
        Entity attacker = e.getDamager();
        String attackerCustomName = attacker.getCustomName();

        Entity defender = e.getEntity();
        String defenderCustomName = defender.getCustomName();

        if (attackerCustomName == null)
            attackerCustomName = "";
        if (defenderCustomName == null)
            defenderCustomName = "";

        WeaponOfPowerData attackerWopData = WeaponOfPowerInventoryBank.WorldBank.getWeaponOfPowerData(attackerCustomName);
        boolean attackerHasWop = attackerWopData != null;

        WeaponOfPowerData defenderWopData = WeaponOfPowerInventoryBank.WorldBank.getWeaponOfPowerData(defenderCustomName);
        boolean defenderHasWop = defenderWopData != null;

        //================================= Cancellation Block ======================================\
        //Note: additional cancel condition below that is not in cancellation block
        boolean attackerPlayer = attacker instanceof Player;
        boolean defenderPlayer = defender instanceof Player;


        LivingEntityData attackerLED  = null;
        LivingEntityData defenderLED  = null;
        if(attackerPlayer || attackerHasWop)
            attackerLED = LivingEntityBank.getLivingEntityData(attacker.getUniqueId());

        if(defenderPlayer || defenderHasWop)
            defenderLED = LivingEntityBank.getLivingEntityData(defender.getUniqueId());

        //================================= Ammo Application Block ======================================
        long currentTime = System.currentTimeMillis();
        if(attackerPlayer)
        {
            Player player = (Player)attacker;
            long swingTimer = currentTime - attackerLED.getLastAttackTime();
            int swingDelay = attackerLED.getAttackDelay();
            if (swingTimer > swingDelay)            //if player's cool-down is finished //(currentTime - attackerLED.getLastWOPRegenTime()) > 100)                                    //and 100ms since last WOP regen (to prevent making too many)                                    //and 100ms since last WOP regen (to prevent making too many)
            {
                attackerLED.setLastAttackTime(currentTime);//then reset player cool-down period
                if(attackerWopData.getInfiniteLevel() < 1)
                {
                    int fragilityLevel = attackerWopData.getSplashLevel(); //PowerID:28 = FRAGILE
                    int additionalAmmo = fragilityLevel < 0 ? fragilityLevel * -1 : 0;
                    int currentAmmo = attackerWopData.getAmmo();

                    if (!attackerWopData.removeAmmo(1 + additionalAmmo))//if not enough ammo
                    {
                        e.setCancelled(true);//cancel the event
                        return;
                    } else //otherwise, remove durability (ammo)
                    {
                        System.out.println("Damage: " + currentAmmo + " -> " + attackerWopData.getAmmo());
                        WeaponsOfPower.NSA.refreshChargesArtifact(player);
                        //===================================== Interact: Regen ===========================================
                        //only start a new regen ammo recursion if current damage is 0 and item has appropriate lore
                        if (currentAmmo == attackerWopData.getMaxAmmo() && attackerWopData.getAmmoRegenLevel() != 0)
                        {
                            System.out.println("Regen Timer Started From Player Interact.");
                            new Regen_Ammo(attackerWopData, attackerLED).runTaskLater(WeaponsOfPower.PLUGIN, 20);
                        }
                    }
                }
            }
        }

        //================================= Damage Application Block ======================================

        int attackerLevel;
        int defenderLevel;
        //get levels and set LEDs
        if (attackerPlayer) //attacker is player.
        {   //attacker level is in LED
            attackerLevel = attackerLED.getLevel();
            defenderLevel = defenderHasWop ? (int) WeaponsOfPower.MONSTER_LEVEL_AVERAGE : attackerLevel;
        }
        else //attacker is a mob with an wop iron sword, defender is a player. either or both attacker and defender have a wop
        {   //defender is a player
            defenderLevel = defenderLED.getLevel();
            attackerLevel = attackerHasWop ? (int) WeaponsOfPower.MONSTER_LEVEL_AVERAGE : defenderLevel;
        }

        double levelRatioModifier = 1 + (attackerLevel - defenderLevel) * 0.01;

        int protectionLevel = defenderHasWop ? defenderWopData.getProtectionLevel() : 0 ;
        double protectionModifier = 1 - protectionLevel * .1;

        int damageLevel = attackerHasWop ? attackerWopData.getDamageLevel() : 0;
        double damageIncreaseModifier = 1 + damageLevel * 0.1;

        int wopBaseDamage = 0;
        if (attackerHasWop && defenderHasWop)
        {
            int attackerBase = attackerLED.getBaseATK();
            int defenderBase = defenderLED.getBaseDEF();
            wopBaseDamage = attackerBase - defenderBase;
        }

        double damage = wopBaseDamage + e.getDamage() * levelRatioModifier * protectionModifier * damageIncreaseModifier;
        e.setDamage(damage);
        System.out.println(attacker.getName() + " dealt " + damage + " damage to " + defender.getName());

        //================================= Vamp/Charity Block ======================================
        int vampLevel = attackerHasWop ?  attackerWopData.getLifeStealLevel() : 0;
        LivingEntity livingEntity = null;
        AttributeInstance maxHealthObj = null;
        double maxHealth = 20.0; //20 is the default max hp
        if (vampLevel != 0)  //vamp/charity
        {
            livingEntity = (LivingEntity) attacker;
            maxHealthObj = livingEntity.getAttribute(Attribute.GENERIC_MAX_HEALTH);
            if (maxHealthObj != null) maxHealth = maxHealthObj.getValue();
            double newHealth = livingEntity.getHealth() + vampLevel * .1 * damage;
            if(newHealth > maxHealth)
                newHealth = maxHealth;
            else if(newHealth < 0)
                newHealth = 0;
            livingEntity.setHealth(newHealth);
        }

        //================================= Regen Timer Starter ======================================
        livingEntity = (LivingEntity)defender;
        double finalHealth = livingEntity.getHealth() - e.getFinalDamage();
        maxHealthObj = livingEntity.getAttribute(Attribute.GENERIC_MAX_HEALTH);
        maxHealth = (maxHealthObj != null) ? maxHealthObj.getValue() : 20.0;
        if (defenderHasWop && defenderWopData.getLifeRegenLevel() > 0 && finalHealth < maxHealth)
            new Regen_Health(livingEntity).runTaskLater(WeaponsOfPower.PLUGIN, 20);

        //================================= Poison Block ======================================
        int poisonLevel = attackerHasWop ? attackerWopData.getPoisonLevel() : 0;
        if (poisonLevel > 0)
        {   //create a new LED to poison target
            if (defenderLED == null)
            {
                defenderLED = new LivingEntityData(defender.getUniqueId());
                LivingEntityBank.addLivingEntityData(defender.getUniqueId(), defenderLED);
            }

            defenderLED.setPoisonTime((poisonLevel+1)/2, currentTime + 5000);
            if(!defenderLED.isPoisoned())
            {
                defenderLED.setPoisoned(true);
                new PoisonTimer(defenderLED).runTaskLater(WeaponsOfPower.PLUGIN, 10);
            }
        }

        //================================= Entity vs Entity: Boom ======================================
        int roll = WeaponsOfPower.RANDOM.nextInt(5);
        int explosiveLevel = attackerHasWop ? attackerWopData.getExplosiveLevel() : 0;
        if ( explosiveLevel > roll)//PowerID:8 = BOOM
        {
            Location location = defender.getLocation(); //explode where the player is looking
            //note this only handles melee atm
            Fireball fireball = (Fireball) attacker.getWorld().spawnEntity(location, EntityType.FIREBALL); //fireball had the more control and aesthetics than creeper or tnt. Could not use world.createExplosion(), needed way to track damage back to source entity
            fireball.setCustomName(String.valueOf(attackerWopData.getHashCode())); //provides way to track back to entity
            fireball.setYield(2);
            fireball.setIsIncendiary(false);
            fireball.setVelocity(new Vector(0, -1000, 0)); //sends straight down fast enough to explode immediately
        }
    }
}