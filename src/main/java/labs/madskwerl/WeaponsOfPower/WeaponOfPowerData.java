package labs.madskwerl.WeaponsOfPower;

import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;


public class WeaponOfPowerData
{
    
    private ItemStack itemStack = null; //holds the associated itemstack
    private ItemMeta itemMeta = null;
    private Material materialType = null;
    private int hashCode = this.hashCode(); //used as a link in hashmaps
    private Entity entity = null;
    private boolean entityIsPlayer = false;
    private int ammo = 99;
    private int maxAmmo = 99;

    private int[] powerLevels = new int[Powers.NumberOfPowers];

    private int slots = 0;
    private int slotsUsed = 0;

    private boolean ammoRegenPrimed = false;

    public WeaponOfPowerData(int powerID, int powerLevel)
    {
        this.newWOP(null, powerID, powerLevel, null);
    }

    public WeaponOfPowerData(ItemStack itemStack, int powerID, int powerLevel)
    {
        this.newWOP(itemStack, powerID, powerLevel, null);
    }

    public WeaponOfPowerData(ItemStack itemStack, int powerID, int powerLevel, Entity entity)
    {
        this.newWOP(itemStack, powerID, powerLevel, entity);
    }

    public boolean setPlayer(Entity player)
    {
        if(player instanceof Player)
        {
            this.entity = player;
            return this.entityIsPlayer = true;
        }
        return  false;
    }

    public void setEntity(Entity entity)
    {
        this.entity = entity;
        this.entityIsPlayer = entity instanceof Player;
    }

    public boolean entityIsPlayer()
    {
        return entityIsPlayer;
    }

    public void addAmmo(int amount)
    {
        if(amount > 0)
        {
            if (this.ammo + amount > this.maxAmmo)
                this.ammo = this.maxAmmo;
            else
                this.ammo += amount;
        }
    }

    public boolean removeAmmo(int amount)
    {
        if(amount > 0)
        {
            if(this.ammo - amount > -1)
            {
                this.ammo -= amount;
                return true;
            }
        }
        return false;
    }

    public int getAmmo()
    {
        return this.ammo;
    }


    public Player getPlayer()
    {
        return (this.entityIsPlayer) ? (Player)this.entity : null;
    }

    public Entity getEntity()
    {
        return this.entity;
    }

    public boolean setPowerLevel(int powerID, int powerLevel)
    {
        int powerLimitHigh;
        int powerLimitLow;
       if(powerID == -1) //power slots
       {
           powerLimitHigh = 25;
           powerLimitLow = -10;
       }
       else if(powerID == 0) //infinite power level
       {
           powerLimitHigh = 1;
           powerLimitLow = -10;
       }
       else if(powerID > 0 && powerID < 29)
       {
           powerLimitHigh = 10;
           powerLimitLow = -10;
       }
       else if(powerID == 29) //retention level (0 or 1)
       {
           powerLimitHigh = 1;
           powerLimitLow = 0;

       }
       else if(powerID == 30) //healing power level > 0
       {
           powerLimitHigh = 10;
           powerLimitLow = 0;
       }
       else
       {
           powerLimitHigh = 0;
           powerLimitLow = 0;
       }

       if(powerLevel > powerLimitHigh || powerLevel < powerLimitLow)
       {
           return false;
       }
       else
       {
           if(powerID == -1)
               this.slots = powerLevel;
           else
               this.powerLevels[powerID] = powerLevel;
           return true;
       }
    }

    public int getPowerLevel(int powerID)
    {
        if(powerID == -1)
            return this.slots;
        else if(powerID > -1 && powerID < this.powerLevels.length)
            return this.powerLevels[powerID];
        else
        {
            System.out.println("WeaponOfPowerData.getPowerLevel() - PowerID: " + powerID + ". Provided level out of valid range.");
            return 0;
        }
    }
    
    public int getSlots()
    {
        return this.slots;
    }

    public int getInfiniteLevel()
    {
        return this.powerLevels[0];
    }

    public int getAmmoRegenLevel()
    {
        return this.powerLevels[1];
    }

    public int getLifeStealLevel()
    {
        return this.powerLevels[2];
    }

    public int getLifeRegenLevel()
    {
        return this.powerLevels[3];
    }

    public int getDamageLevel()
    {
        return this.powerLevels[4];
    }

    public int getProtectionLevel()
    {
        return this.powerLevels[5];
    }

    public int getPoisonLevel()
    {
        return this.powerLevels[6];
    }

    public int getPoisonProtectionLevel()
    {
        return this.powerLevels[7];
    }

    public int getExplosiveLevel()
    {
        return this.powerLevels[8];
    }

    public int getExplosiveProtectionLevel()
    {
        return this.powerLevels[9];
    }

    public int getSlowLevel()
    {
        return this.powerLevels[10];
    }

    public int getSlowProtectionLevel()
    {
        return this.powerLevels[11];
    }

    public int getSpeedBoostLevel()
    {
        return this.powerLevels[12];
    }

    public int getFireChanceLevel()
    {
        return this.powerLevels[13];
    }

    public int getFireProtectionLevel()
    {
        return this.powerLevels[14];
    }

    public int getInstagibChanceLevel()
    {
        return this.powerLevels[15];
    }

    public int getInstagibProtectionLevel()
    {
        return this.powerLevels[16];
    }

    public int getStunChanceLevel()
    {
        return this.powerLevels[17];
    }

    public int getStunProtectionLevel()
    {
        return this.powerLevels[18];
    }

    public int getAtkSpeedBoostLevel()
    {
        return this.powerLevels[19];
    }

    public int getAtkSpeedProtectionLevel()
    {
        return this.powerLevels[20];
    }

    public int getJumpBoostLevel()
    {
        return this.powerLevels[21];
    }

    public int getFallProtectionLevel()
    {
        return this.powerLevels[22];
    }

    public int getCharmChanceLevel()
    {
        return this.powerLevels[23];
    }

    public int getInvisibilityLevel()
    {
        return this.powerLevels[24];
    }

    public int getKnockBackLevel()
    {
        return this.powerLevels[25];
    }

    public int getPullLevel()
    {
        return this.powerLevels[26];
    }

    public int getSturdyLevel()
    {
        return this.powerLevels[27];
    }

    public int getSplashLevel()
    {
        return this.powerLevels[28];
    }

    public int getRetentionLevel()
    {
        return this.powerLevels[29];
    }

    public int getHealingLevel()
    {
        return this.powerLevels[30];
    }


    public int getHashCode()
    {
        return this.hashCode;
    }

    static int getMaxAmmo(Material material)
    {
        switch(material)
        {
            case IRON_SWORD:
            case BOW:
            case SNOWBALL:
                return  99;

            default:
                return 0;
        }
    }

    public int getMaxAmmo()
    {
        return this.maxAmmo;
    }

    public void newWOP(ItemStack itemStack, int powerID, int powerLevel, Entity entity)
    {
        this.updateItemStack(itemStack);
        this.clearPowerLevels();
        this.setPowerLevel(powerID, powerLevel);
        if(this.itemStack != null)
        {
            this.updateLore();
            this.updateDisplayName(powerID, powerLevel);
        }
        this.setEntity(entity);
    }

    private void updateItemStack(ItemStack itemStack)
    {
        if(itemStack != null)
        {
            this.itemStack = itemStack;
            this.itemMeta = itemStack.getItemMeta();
            this.itemMeta.setLocalizedName(Integer.toString(this.hashCode));
            this.itemStack.setItemMeta(this.itemMeta);
            this.materialType = itemStack.getType();
        }
    }

    private void updateLore()
    {
        List<String> loreList = new ArrayList<>();
        for(int i = 0; i < this.powerLevels.length; i++)
        {
            int powerLevel = this.powerLevels[i];
            if(powerLevel != 0)
                loreList.add(Powers.getName(i, powerLevel) + " " + powerLevel);
        }
        this.itemMeta.setLore(loreList);
        this.itemStack.setItemMeta(this.itemMeta);
    }

    private void updateDisplayName(int powerID, int powerLevel)
    {
        String materialName = itemStack.getType().toString().replace("_", " ");
        this.itemMeta.setDisplayName(Powers.getPrefix(powerID, powerLevel) + materialName +
                                     Powers.getSuffix(powerID, powerLevel) + " " + powerLevel);
        itemStack.setItemMeta(itemMeta);
    }

    private void clearPowerLevels()
    {
        this.slots = 0;
        this.powerLevels = new int[Powers.NumberOfPowers];
    }

    public Material getMaterialType()
    {
        return this.materialType;
    }

    public boolean addPowerUp(PowerUpData powerUpData)
    {
        if (this.slots < 1)
        {
            System.out.println("WeaponOfPowerData.addPowerUp() - Weapon of power level to low to add power up.");
            return false;
        } else if (this.slotsUsed == this.slots)
        {
            System.out.println("WeaponOfPowerData.addPowerUp() - Not enough slots to add power up.");
            return false;
        } else
        {
            int powerLevel;
            this.slotsUsed++;
            if (powerUpData.powerID == -1)
                powerLevel = this.slots + powerUpData.powerLevel;
            else
                powerLevel = this.powerLevels[powerUpData.powerID] + powerUpData.powerLevel;
            boolean successfullyAdded = this.setPowerLevel(powerUpData.powerID, powerLevel);
            if (!successfullyAdded)
                System.out.println("WeaponOfPowerData.addPowerUp() - Cannot add another power up of this type");
            return successfullyAdded;
        }
    }

    public boolean ammoRegenPrimed()
    {
        return this.ammoRegenPrimed;
    }

    public void primeAmmoRegen()
    {
        this.ammoRegenPrimed = true;
    }

    public void unprimeAmmoRegen()
    {
        this.ammoRegenPrimed = false;
    }
}
