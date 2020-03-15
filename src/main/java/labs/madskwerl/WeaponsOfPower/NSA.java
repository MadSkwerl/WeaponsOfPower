package labs.madskwerl.WeaponsOfPower;

import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.block.banner.Pattern;
import org.bukkit.block.banner.PatternType;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BannerMeta;
import org.bukkit.inventory.meta.ItemMeta;
import java.util.List;

public class NSA implements Listener
{
    static boolean MobsDropPowerups = true;

    public NSA()
    {
        WeaponsOfPower.PLUGIN.getServer().getPluginManager().registerEvents(this, WeaponsOfPower.PLUGIN);
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e)
    {
        //First check if the player is sneaking
        Player player = e.getPlayer();
        if(player.isSneaking())
        {
            int heldItemSlot = player.getInventory().getHeldItemSlot();
            LivingEntityData playerLED = LivingEntityBank.getLivingEntityData(player.getUniqueId());
            if(heldItemSlot == 0)
                playerLED.scrollPowerInventory(LivingEntityData.Scroll.LEFT);
            else if(heldItemSlot == 8)
                playerLED.scrollPowerInventory(LivingEntityData.Scroll.RIGHT);
            e.setCancelled(true);
            return;
        }
        //Calls onUse for the specific wop
        Action action = e.getAction();
        String wopHashCode = player.getCustomName();
        if (wopHashCode == null)
            wopHashCode = "";
        WeaponOfPowerData wopData = WeaponOfPowerInventoryBank.WorldBank.getWeaponOfPowerData(wopHashCode);
        boolean playerHasWop = wopData != null;

        if (playerHasWop && (action.equals(Action.LEFT_CLICK_AIR) || action.equals(Action.LEFT_CLICK_BLOCK)))
        {
            if (wopData.getMaterialType() == Material.IRON_SWORD)
                WOP_IRON_SWORD.onUse(e);
            else if (wopData.getMaterialType() == Material.BOW)
                WOP_BOW.onUse(e);
        }
    }

    @EventHandler
    public void onEntityDamageByEntityEvent(EntityDamageByEntityEvent e)
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

        boolean oneIsWOP = attackerHasWop || defenderHasWop;



        //boolean attackerRegMob = attackerWopData == null && !(attacker instanceof Player);
        //boolean attackerWopMob = attackerWopData != null && !attackerWopData.entityIsPlayer();
        boolean attackerRegPlayer  = attackerWopData == null && (attacker instanceof Player);
        boolean attackerWopPlayer  = attackerWopData != null && attackerWopData.entityIsPlayer();
        //boolean defenderRegMob = defenderWopData == null && !(defender instanceof Player);
        boolean defenderWopMob = defenderWopData != null && !defenderWopData.entityIsPlayer();
        boolean defenderRegPlayer = defenderWopData == null && (defender instanceof Player);
        boolean defenderWopPlayer = defenderWopData != null && defenderWopData.entityIsPlayer();

        boolean attackerPlayer = attackerRegPlayer || attackerWopPlayer;
        boolean defenderPlayer = defenderRegPlayer || defenderWopPlayer;
        boolean bothNotPlayers = !attackerPlayer && !defenderPlayer;
        boolean bothPlayers = attackerPlayer && defenderPlayer;
        boolean attackerWopPlayerHealing = attackerWopPlayer && attackerWopData.getHealingLevel() > 0;

        //lets server do normal damages if wop is not involved
        if(!oneIsWOP || (attackerRegPlayer && defenderWopPlayer))
        {
            return;
        }

        //prevents wop players from damaging each other
        //prevents wop mobs from damaging or being damaged by reg mobs
        //prevents wop mobs from damaging each other
        //prevents players w/o wop from damaging wop mobs

        if(bothNotPlayers || (attackerRegPlayer && defenderWopMob) || bothPlayers && !attackerWopPlayerHealing)
        {
            e.setCancelled(true);
            return;
        }



        if (e.getCause().equals(EntityDamageEvent.DamageCause.ENTITY_EXPLOSION))
            WOP_EXPLOSION.onHit(e);
        else if (attackerHasWop && attackerWopData.getMaterialType() == Material.IRON_SWORD)
            WOP_IRON_SWORD.onHit(e);
        else if (attackerHasWop && attackerWopData.getMaterialType() == Material.BOW)
        {
            if (attacker instanceof Projectile)
                WOP_BOW.projectile_onHit(e);
            else
                WOP_BOW.onHit(e);
        }
        else if (defenderHasWop)//attacker is non-wop & non-explosive
        {
            int protectionLevel = defenderWopData.getProtectionLevel();
            double protectionModifier = 1 - protectionLevel * .1;
            e.setDamage(e.getDamage() * protectionModifier);
            System.out.println(attacker.getName() + " dealt " + e.getDamage() + " damage to " + defender.getName());
            //handle defender hp regen
            LivingEntity livingEntity = (LivingEntity)defender;
            AttributeInstance maxHealthObj = livingEntity.getAttribute(Attribute.GENERIC_MAX_HEALTH);
            double maxHealth = (maxHealthObj != null) ? maxHealthObj.getValue() : 20.0;
            double finalHealth = livingEntity.getHealth() - e.getFinalDamage();
            if (defenderWopData.getLifeRegenLevel() > 0 && finalHealth < maxHealth)
            {
                System.out.println("Regen HP Timer Started From Normal Damage.");
                new Regen_Health(livingEntity).runTaskLater(WeaponsOfPower.PLUGIN, 20);
            }
        }
    }

    @EventHandler
    public void EntityDamageEvent(EntityDamageEvent e)
    {
        EntityDamageEvent.DamageCause cause = e.getCause();
        Entity entity = e.getEntity();
        String entityCustomName = entity.getCustomName();
        if (entityCustomName == null)
            entityCustomName = "";

        WeaponOfPowerData entityWopData = WeaponOfPowerInventoryBank.WorldBank.getWeaponOfPowerData(entityCustomName);
        boolean entityHasWop = entityWopData != null;

        if(entityHasWop)
        {
            int protectionLevel = entityWopData.getProtectionLevel();
            int fireProtectionLevel = entityWopData.getFireProtectionLevel();
            int poisonProtectionLevel = entityWopData.getPoisonProtectionLevel();
            double modifier = 1;

            switch (cause)
            {
                case FIRE:
                case FIRE_TICK:
                case HOT_FLOOR:
                case LAVA:
                case DRAGON_BREATH:
                    modifier -= fireProtectionLevel * 0.1;
                    modifier -= protectionLevel *0.1;
                break;
                case POISON:
                    modifier -= poisonProtectionLevel;
                    modifier -= protectionLevel *0.1;
                break;

                case CONTACT:
                case FALLING_BLOCK:
                case FLY_INTO_WALL:
                case THORNS:
                case LIGHTNING:
                    modifier -= protectionLevel * 0.1;
                break;
            }

            double damage = e.getDamage()  * modifier;

            //================================= Regen Timer Starter ======================================
            LivingEntity livingEntity = (LivingEntity)entity;
            LivingEntityData livingEntityData = LivingEntityBank.getLivingEntityData(entity.getUniqueId());

            //If no living entity data for entity exist create new one
            if(livingEntityData == null)
            {
                livingEntityData  = new LivingEntityData(entity.getUniqueId());
                LivingEntityBank.addLivingEntityData(entity.getUniqueId(), livingEntityData);
            }
            double finalHealth = livingEntity.getHealth() - e.getFinalDamage();
            AttributeInstance maxHealthObj = livingEntity.getAttribute(Attribute.GENERIC_MAX_HEALTH);
            double maxHealth = (maxHealthObj != null) ? maxHealthObj.getValue() : 20.0;
            if (entityWopData.getLifeRegenLevel() > 0 && finalHealth < maxHealth && !livingEntityData.isRegenHealth())
            {
                livingEntityData.setRegenHealth(true);
                new Regen_Health(livingEntity).runTaskLater(WeaponsOfPower.PLUGIN, 20);
                System.out.println("Regen HP Started from Non-entity Damage");
            }

            e.setDamage(damage);
            System.out.println(e.getCause() + " dealt " + damage + " damage to " + entity.getName());
        }
    }

    @EventHandler
    public void onPlayerItemDamageEvent(PlayerItemDamageEvent e)
    {
        //================= Cancel Durability Loss ===========================
        try
        {
            String hashcode = e.getItem().getItemMeta().getLocalizedName();
            if (WeaponOfPowerInventoryBank.isWop(hashcode))//if the damaged item is a WOP
                e.setCancelled(true);//cancel the durability loss
        } catch (Exception err)
        {
            System.out.println("Error onPlayerItemDamageEvent");
        }
        //======= End Durability Loss ====
    }

    @EventHandler
    public void onPlayerItemHeldEvent(PlayerItemHeldEvent e)
    {
        //System.out.println("PlayerItemHeldEvent Triggered");
        Player player = e.getPlayer();
        String localizedName = "";
        float speed = 0.2f;
        try
        {
            //set player customName to wop localizedName
            localizedName = player.getInventory().getItem(e.getNewSlot()).getItemMeta().getLocalizedName();
            WeaponOfPowerData wopData = WeaponOfPowerInventoryBank.WorldBank.getWeaponOfPowerData(localizedName);
            boolean itemIsWop = wopData != null;

            //handle switching to a wop with health regen
            if (itemIsWop)
            {
                LivingEntityData livingEntityData = LivingEntityBank.getLivingEntityData(e.getPlayer().getUniqueId());
                if (livingEntityData != null && !livingEntityData.isRegenHealth() && wopData.getLifeRegenLevel() != 0)
                {
                    livingEntityData.setRegenHealth(true);
                    System.out.println("Regen HP Timer Started From ItemHeldEvent");
                    new Regen_Health(e.getPlayer()).runTaskLater(WeaponsOfPower.PLUGIN, 20);
                }
                int speedLevel = wopData.getSpeedBoostLevel();
                speed = speedLevel < 0 ? .2f + .02f * speedLevel : 0.2f + .05f * speedLevel;
            }
        } catch (Exception err)
        {
            System.out.println("ItemHeldEventError");
        }
        player.setCustomName(localizedName);
        player.setWalkSpeed(speed);
        new Delayed_RefreshChargesArtifact(e.getPlayer()).runTaskLater(WeaponsOfPower.PLUGIN, 1);
    }


    @EventHandler
    public void onPlayerDropItemEvent(PlayerDropItemEvent e)
    {
        try
        {
            ItemMeta itemMeta = e.getItemDrop().getItemStack().getItemMeta();
            String localizedName = (itemMeta != null) ? itemMeta.getLocalizedName() : "";
            WeaponOfPowerData wopData = WeaponOfPowerInventoryBank.WorldBank.getWeaponOfPowerData(localizedName);
            boolean isWOP = wopData != null;
            LivingEntityData livingEntityData = LivingEntityBank.getLivingEntityData(e.getPlayer().getUniqueId());

            if(wopData != null)
            {
                wopData.setEntity(null);
            }
            else if(localizedName.contains("WEAPON_POWER_UP"))
            {
                livingEntityData.removePowerUp(e.getItemDrop().getItemStack());
                e.getItemDrop().getItemStack().setAmount(1);
            }
            else if(localizedName.contains("CHARGES_ARTIFACT"))
            {
                e.getItemDrop().remove();
                livingEntityData.getChargesArtifact().setAmount(0);
                livingEntityData.setChargesArtifact(null);
            }

            //refresh charges artifact if dropped item was held item and a wop
            Player player = e.getPlayer();
            if(isWOP && localizedName.equalsIgnoreCase(player.getCustomName()))
                this.refreshChargesArtifact(player);
            if (player.getInventory().getItemInMainHand().getType().equals(Material.AIR))
                player.setCustomName("");
        }
        catch (Exception err)
        {
            System.out.println("DroppedItemException");
        }
    }

    @EventHandler
    public void onEntityPickupItemEvent(EntityPickupItemEvent e)
    {
        //System.out.println("EntityPickupItemEvent Triggered");
        try
        {
            Player player = (Player) e.getEntity();
            LivingEntityData livingEntityData = LivingEntityBank.getLivingEntityData(player.getUniqueId());
            ItemStack itemStack = e.getItem().getItemStack(); //Object is a copy of what will be put in the players inventory
            String localizedName = itemStack.getItemMeta().getLocalizedName();

            WeaponOfPowerData wopData = WeaponOfPowerInventoryBank.WorldBank.getWeaponOfPowerData(localizedName);
            boolean itemIsWop = wopData != null;

            if(localizedName.contains("WEAPON_POWER_UP"))
            {
                livingEntityData.addPowerUp(e.getItem().getItemStack());
                e.getItem().remove();
                e.setCancelled(true);
            }
            else if (itemIsWop)
            {
                wopData.setPlayer(player);
                if (player.getInventory().getHeldItemSlot() == player.getInventory().firstEmpty())
                {
                    player.setCustomName(localizedName);
                    if(livingEntityData.hasChargesArtifact())
                    {
                        new Delayed_RefreshChargesArtifact(player).runTaskLater(WeaponsOfPower.PLUGIN, 1);
                    }
                }
                if (wopData.getAmmoRegenLevel() != 0)
                {
                    wopData.primeAmmoRegen();
                    new Regen_Ammo_Primer(player).runTaskLater(WeaponsOfPower.PLUGIN, 1);
                }
                else if (wopData.getLifeRegenLevel() != 0 && !livingEntityData.isRegenHealth())
                {
                    livingEntityData.setRegenHealth(true);
                    System.out.println("Regen HP Timer Started From ItemPickupEvent");
                    new Regen_Health(player).runTaskLater(WeaponsOfPower.PLUGIN, 20);
                }
            }
        } catch (Exception err)
        {
            System.out.println("PickUpEventError");
        }
    }

    //called by regen_ammo BukkitRunnable (initially onPlayerInteract, recursively through regenAmmo)
    public void regenAmmo(WeaponOfPowerData wopData, LivingEntityData livingEntityData)
    {
        try
        {
            boolean isStillInWopBank = WeaponOfPowerInventoryBank.isWop(wopData.getHashCode());
            if (isStillInWopBank && wopData.getPlayer() != null)//if item exists and is in player possession
            {
                int powerLevel = wopData.getAmmoRegenLevel();//PowerID:1 = AMMO REGEN
                int maxAmmo = wopData.getMaxAmmo();//check max damage
                int currentAmmo = wopData.getAmmo();            //and current damage
                boolean hasRegenAndMissingAmmo = (powerLevel > 0 && currentAmmo != maxAmmo);
                boolean hasRobbingAndHasAmmo = (powerLevel < 0 && currentAmmo > 0);
                if (hasRegenAndMissingAmmo || hasRobbingAndHasAmmo)
                {
                    if (powerLevel > 0)
                    {
                        if (wopData.getAmmo() + powerLevel > wopData.getMaxAmmo())
                            powerLevel = wopData.getMaxAmmo() - wopData.getAmmo();
                        wopData.addAmmo(powerLevel);
                    }
                    else
                    {
                        if (wopData.getAmmo() + powerLevel < 0)
                            powerLevel = -wopData.getAmmo();
                        wopData.removeAmmo(-powerLevel);
                    }
                    livingEntityData.setLastWOPRegenTime(System.currentTimeMillis());//timestamp to prevent creating too many regen_ammo tasks
                    this.refreshChargesArtifact((Player)(WeaponsOfPower.PLUGIN.getServer().getEntity(livingEntityData.getUUID())));
                    new Regen_Ammo(wopData, livingEntityData).runTaskLater(WeaponsOfPower.PLUGIN, 20);

                    System.out.println("Damage: " + currentAmmo + " -> " + wopData.getAmmo());
                    System.out.println("Regen Timer Started From Timer.");
                }

            }
        } catch (Exception e)
        {
            System.out.println("Regen timer fail");
        }
    }

    public void fireRegenAmmo(Player player)
    {
        LivingEntityData livingEntityData = LivingEntityBank.getLivingEntityData(player.getUniqueId());
        for (ItemStack itemStack : player.getInventory())
        {
            ItemMeta itemMeta = itemStack.getItemMeta();
            String localizedName = "";
            if (itemMeta != null)
                localizedName = itemMeta.getLocalizedName();

            WeaponOfPowerData wopData = WeaponOfPowerInventoryBank.WorldBank.getWeaponOfPowerData(localizedName);
            boolean itemIsWOp = wopData != null;
            if (itemIsWOp && wopData.ammoRegenPrimed())
            {
                wopData.unprimeAmmoRegen();
                this.regenAmmo(wopData, livingEntityData);
                break;
            }
        }
    }

    public void regenHealth(LivingEntity livingEntity)
    {
        LivingEntityData livingEntityData = LivingEntityBank.getLivingEntityData(livingEntity.getUniqueId());
        if (livingEntityData != null)
        {
            try
            {
                String customName = livingEntity.getCustomName();
                WeaponOfPowerData wopData =WeaponOfPowerInventoryBank.WorldBank.getWeaponOfPowerData(customName);
                double maxHP = livingEntity.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
                double currentHP = livingEntity.getHealth();
                int regenLevel = wopData.getLifeRegenLevel();
                if ((regenLevel > 0 && currentHP < maxHP) || (regenLevel < 0 && currentHP > 0.5))
                {
                    double newHP = currentHP + regenLevel;
                    if (newHP > maxHP)
                        newHP = maxHP;
                    else if (newHP < 0.5)
                        newHP = 0.5;
                    livingEntity.setHealth(newHP);
                    System.out.println("Regen HP started from timer");
                    new Regen_Health(livingEntity).runTaskLater(WeaponsOfPower.PLUGIN, 20);
                } else
                    livingEntityData.setRegenHealth(false);
            } catch (Exception e)
            {
                livingEntityData.setRegenHealth(false);
            }
        }
    }

    public void initPlayer(Player player)
    {
        //Regen_Ammo init
        LivingEntityData livingEntityData = labs.madskwerl.WeaponsOfPower.LivingEntityBank.getLivingEntityData(player.getUniqueId());

        try
        {
            player.setCustomName(player.getInventory().getItemInMainHand().getItemMeta().getLocalizedName());
        }catch (Exception e)
        {
            System.out.println("initPlayer: CustomName not set");
            player.setCustomName("");
        }

        for(ItemStack itemStack : player.getInventory().getContents())
        {
            ItemMeta itemMeta = (itemStack != null) ? itemStack.getItemMeta() : null;
            String localizedName = (itemMeta != null) ? itemStack.getItemMeta().getLocalizedName() : "";
            WeaponOfPowerData wopData = WeaponOfPowerInventoryBank.WorldBank.getWeaponOfPowerData(localizedName);
            if (wopData != null)
            {
                if (wopData.getAmmoRegenLevel() != 0)
                {
                    wopData.primeAmmoRegen();
                    new Regen_Ammo(wopData, livingEntityData).runTaskLater(WeaponsOfPower.PLUGIN, 1);
                }
            }
            else if(localizedName.contains("CHARGES_ARTIFACT"))
            {
                livingEntityData.setChargesArtifact(itemStack);
                refreshChargesArtifact(player);
            }
        }

        String customName = player.getCustomName();
        WeaponOfPowerData wopData = WeaponOfPowerInventoryBank.WorldBank.getWeaponOfPowerData(customName);

        if (wopData != null && wopData.getLifeRegenLevel() != 0 && !livingEntityData.isRegenHealth()) //PowerID:3 = REGEN
        {
            livingEntityData.setRegenHealth(true);
            System.out.println("Regen HP Timer Started From initPlayer");
            new Regen_Health(player).runTaskLater(WeaponsOfPower.PLUGIN, 20);
        }

        int speedLevel = (wopData != null) ? wopData.getSpeedBoostLevel() : 0;
        float speed = speedLevel < 0 ? .2f + .02f * speedLevel : 0.2f + .05f * speedLevel;
        player.setWalkSpeed(speed);
    }

    @EventHandler
    public void onPlayerLoginEvent(PlayerLoginEvent e)
    {
        Player player = e.getPlayer();
        player.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(20);
        player.setHealthScale(20);
        LivingEntityData livingEntityData = LivingEntityBank.getLivingEntityData(player.getUniqueId());
        if (livingEntityData == null)
            LivingEntityBank.addLivingEntityData(player.getUniqueId(), new PlayerData(player.getUniqueId()));
        try
        {
            this.initPlayer(player);
        }catch(Exception err){}
    }

    @EventHandler
    public void onPlayerLogoffEvent(PlayerQuitEvent e)
    {
        LivingEntityBank.removeLivingEntityData(e.getPlayer().getUniqueId());
        for (ItemStack itemStack : e.getPlayer().getInventory())
        {
            ItemMeta itemMeta = itemStack.getItemMeta();
            String localizedName = (itemMeta == null) ? "" : itemMeta.getLocalizedName();
            WeaponOfPowerInventoryBank.WorldBank.removeWeaponOfPowerData(Integer.parseInt(localizedName));
        }
    }

    public void bindChargesArtifact(Player player)
    {
        //Scans player inventory and and binds the first found (should only be 1) artifact to the player LivingEntityData
        //refreshCharges Artifact is then called (b/c this should only be called when adding a charge object to player inv)
        LivingEntityData livingEntityData  = labs.madskwerl.WeaponsOfPower.LivingEntityBank.getLivingEntityData(player.getUniqueId());
        for(ItemStack itemStack : player.getInventory().getContents())
        {
            if(itemStack != null)
            {
                ItemMeta itemMeta = itemStack.getItemMeta();
                String localizedNamed;
                if (itemMeta != null)
                    localizedNamed = itemMeta.getLocalizedName();
                else
                    localizedNamed = "";

                if (localizedNamed.contains("CHARGES_ARTIFACT"))
                {
                    livingEntityData.setChargesArtifact(itemStack);
                    break;
                }
            }
        }
        this.refreshChargesArtifact(player);
    }

    public void refreshChargesArtifact(Player player)
    {
        ItemStack chargesArtifact = LivingEntityBank.getLivingEntityData(player.getUniqueId()).getChargesArtifact();
        //return if player is crouching or does not have charges artifact
        if(player.isSneaking() || chargesArtifact == null)
            return;

        ItemStack itemStackInMainHand = player.getInventory().getItemInMainHand();
        ItemMeta itemMetaInMainHand = itemStackInMainHand.getItemMeta();
        String localizedName = (itemMetaInMainHand == null) ? "" : itemMetaInMainHand.getLocalizedName();

        WeaponOfPowerData wopData = WeaponOfPowerInventoryBank.WorldBank.getWeaponOfPowerData(localizedName);

        Material baseColor = Material.BLACK_BANNER;
        int charges = 1;
        if(wopData != null)
        {
            //calc charges
            charges = wopData.getAmmo();

            //determine color, base on range of 100 charges
            if(charges > 75) //Green:100-76 Yellow:75-51 Orange:50-26 Red:25-1 Black:0
            {
                baseColor = Material.LIME_BANNER;
            }
            else if(charges > 50)
            {
                baseColor = Material.YELLOW_BANNER;
            }
            else if(charges > 25)
            {
                baseColor = Material.ORANGE_BANNER;
            }
            else if(charges > 0)
            {
                baseColor = Material.RED_BANNER;
            }

            //handle under and over flow conditions
            if(charges > 100)
                charges = 100;
            else if(charges < 1)
                charges = 1;
        }
        //set amount and color
        chargesArtifact.setAmount(charges);
        chargesArtifact.setType(baseColor);
        player.updateInventory();
    }


    @EventHandler
    public void onInventoryClickEvent(InventoryClickEvent e)
    {
        //Debug Code
        System.out.println("Inventory Click Event");
        System.out.println("Action: " + e.getAction());
        System.out.println("Click: " + e.getClick());
        System.out.println("Clicked Inventory: " + e.getClickedInventory());
        System.out.println("Current Item: " + e.getCurrentItem());
        System.out.println("Cursor: " + e.getCursor());
        System.out.println("Handlers: " + e.getHandlers());
        System.out.println("Hotbar Button: " + e.getHotbarButton());
        System.out.println("Raw Slot: " + e.getRawSlot());
        System.out.println("Slot: " + e.getSlot());
        //Handles moving of Charges Artifact around inventory
        try
        {
            InventoryAction action = e.getAction();
            // If it is a pickup event
            if (action.equals(InventoryAction.PICKUP_ALL)  ||
                    action.equals(InventoryAction.PICKUP_HALF) ||
                    action.equals(InventoryAction.PICKUP_SOME) ||
                    action.equals(InventoryAction.PICKUP_ONE)  ||
                    action.equals(InventoryAction.MOVE_TO_OTHER_INVENTORY) ||
                    action.equals(InventoryAction.SWAP_WITH_CURSOR))
            {
                //Reduce the number of charge to 1 to avoid splitting the stacks
                if (e.getCurrentItem().getItemMeta().getLocalizedName().contains("CHARGES_ARTIFACT"))
                    e.getCurrentItem().setAmount(1);
                if(action.equals(InventoryAction.MOVE_TO_OTHER_INVENTORY))
                    new Delayed_BindChargesArtifact((Player)e.getWhoClicked()).runTaskLater(WeaponsOfPower.PLUGIN, 1);
            }
            else if(action.equals(InventoryAction.PLACE_ALL)    ||
                    action.equals(InventoryAction.PLACE_SOME)   ||
                    action.equals(InventoryAction.PLACE_ONE))
            {
                new Delayed_BindChargesArtifact((Player)e.getWhoClicked()).runTaskLater(WeaponsOfPower.PLUGIN, 1);
            }
            else if(action.equals(InventoryAction.HOTBAR_MOVE_AND_READD) || action.equals(InventoryAction.HOTBAR_SWAP))
            {
                e.setCancelled(true);
            }
        }catch (Exception err) {}
    }

    @EventHandler
    public void onInventoryDragEvent(InventoryDragEvent e)
    {
        System.out.println("Inventory Drag Event");
        System.out.println("Cursor: " + e.getCursor());
        System.out.println("Handlers: " + e.getHandlers());
        System.out.println("Inventory Slots: " + e.getInventorySlots());
        System.out.println("New Items: " + e.getNewItems());
        System.out.println("Old Cursor: " + e.getOldCursor());
        System.out.println("Raw Slots: " + e.getRawSlots());
        System.out.println("Type: " + e.getType());
        System.out.println("Who: " + e.getWhoClicked());
        try
        {
            if(e.getOldCursor().getItemMeta().getLocalizedName().contains("CHARGES_ARTIFACT"))
                new Delayed_BindChargesArtifact((Player)e.getWhoClicked()).runTaskLater(WeaponsOfPower.PLUGIN, 1);
        }catch (Exception err){}
    }

    @EventHandler
    public void onBlockPlaceEvent(BlockPlaceEvent e)
    {
        try
        {
            if (e.getItemInHand().getItemMeta().getLocalizedName().contains("CHARGES_ARTIFACT"))
                e.setCancelled(true);
        }catch (Exception err){}
    }

    public void applyPoison(LivingEntityData led)
    {
        long currentTime = System.currentTimeMillis();
        for(int i = 5; i > -1; i--)
        {
            if(i == 0)
                led.setPoisoned(false);
            else if(led.getPoisonTime(i) > currentTime)
            {
                LivingEntity entity =(LivingEntity) WeaponsOfPower.PLUGIN.getServer().getEntity(led.getUUID());
                double newHealth = entity.getHealth() - i;
                if(newHealth < 0)
                {
                    newHealth = 0;
                    led.setPoisoned(false);
                    //onDeath handles removing led
                }
                entity.setHealth(newHealth);
                System.out.println(entity + "Poisoned-Level: " + i + ", HP: " + entity.getHealth());
                break;
            }
        }

        if(led.isPoisoned())
            new PoisonTimer(led).runTaskLater(WeaponsOfPower.PLUGIN, 10);
    }

    @EventHandler
    public void onPlayerToggleSneakEvent(PlayerToggleSneakEvent e)
    {
        Player player = e.getPlayer();
        LivingEntityData playerData = LivingEntityBank.getLivingEntityData(player.getUniqueId());
        if(playerData.inventoryIsSwapped() && player.isSneaking())
        {
            playerData.swapMainInventory();
            this.bindChargesArtifact(player);
        }
        else if(WeaponOfPowerInventoryBank.isWop(player.getCustomName()) && !player.isSneaking())
            playerData.swapPowerInventory();
    }

    //restricts drops
    @EventHandler
    public void onPlayerDeathEvent(PlayerDeathEvent e)
    {

        //TODO: IF THE WOP INV IS THE CURRENT HOW TO HANDLE THIS

        //Scan inv for charges artifact and set he respawn var for it.
        LivingEntityData playerLED = LivingEntityBank.getLivingEntityData(e.getEntity().getUniqueId());
        playerLED.setChargesArtifactSlotOnRespawn(-1); //clears charges artifact respawn
        int i = 0;
        for (ItemStack itemStack : e.getEntity().getInventory().getContents())
        {
            ItemMeta itemMeta = (itemStack != null) ? itemStack.getItemMeta() : null;

            if(itemMeta != null)
            {
                String localizedName = itemStack.getItemMeta().getLocalizedName();
                if(localizedName.contains("CHARGES_ARTIFACT"))
                    playerLED.setChargesArtifactSlotOnRespawn(i); //sets charges artifact respawn to inv number
            }
            i++;
        }

        //Prevent Inventory artifact and charges artifact from dropping
        List<ItemStack> drops = e.getDrops();
        for (ItemStack itemStack : drops)
        {
            ItemMeta itemMeta = null;
            if(itemStack != null)
                itemMeta = itemStack.getItemMeta();
            if(itemMeta != null)
            {
                String localizedName = itemStack.getItemMeta().getLocalizedName();
                if(localizedName.contains("CHARGES_ARTIFACT"))
                    drops.remove(itemStack);
            }
        }
    }

    @EventHandler
    public void onPlayerRespawnEvent(PlayerRespawnEvent e)
    {
        //if player had charges artifact respawn it in their inv
        Player player = e.getPlayer();
        LivingEntityData playerLED = LivingEntityBank.getLivingEntityData(player.getUniqueId());
        int chargesArtifactSlotOnRespawn = playerLED.getChargesArtifactSlotOnRespawn();
        if (chargesArtifactSlotOnRespawn > -1)
        {
            ItemStack itemStack = new ItemStack(Material.YELLOW_BANNER, 1);
            ItemMeta itemMeta = itemStack.getItemMeta();
            itemMeta.setLocalizedName("CHARGES_ARTIFACT");
            itemMeta.setDisplayName("CHARGES ARTIFACT");
            ((BannerMeta)itemMeta).addPattern(new Pattern(DyeColor.GRAY, PatternType.FLOWER));
            itemStack.setItemMeta(itemMeta);
            LivingEntityBank.getLivingEntityData(player.getUniqueId()).setChargesArtifact(itemStack);
            player.getInventory().setItem(chargesArtifactSlotOnRespawn, itemStack);
            new Delayed_BindChargesArtifact(player).runTaskLater(WeaponsOfPower.PLUGIN, 1);
        }
    }

    public void spawnPowerUP(Location location, int powerupID)
    {
        location.getWorld().dropItemNaturally(location, Powers.generatePowerUp(powerupID));
    }

    public void spawnPowerUp(Inventory inventory, int powerupID)
    {
        inventory.addItem(Powers.generatePowerUp(powerupID));
    }
}
