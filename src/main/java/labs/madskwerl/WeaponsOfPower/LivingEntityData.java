package labs.madskwerl.WeaponsOfPower;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.UUID;

public class LivingEntityData
{
    private int attackDelay = 100;
    private long lastAttackTime = 0;
    private long lastWOPRegenTime = 0;
    private boolean isRegenHealth = false;
    private boolean isPoisoned = false;
    private long[] poisonTime = new long[6]; // holds timer values for poison effect
    private int level = 1;
    private int baseATK = 10;
    private int baseDEF = 10;
    private ItemStack chargesArtifact = null;
    private UUID uuid;

    //holds the slot number the artifact was in on death. -1 If there was none
    private int chargesArtifactSlotOnRespawn = -1;

    /* Power Inventory Variables */
    private ArrayList<ItemStack> powerUps;
    private ItemStack[] backupInventory, powerInventory;
    private int backupCursor, powerCursor;
    private boolean inventoryIsSwapped;
    private int powerIndex;
    public enum Scroll {LEFT, RIGHT}
    public int currentWop = 0; //holds the hashCode of the WeaponOfPowerData that the player has currently equipped. Is 0 if not WOP is equipped


    public LivingEntityData(UUID uuid)
    {
        this.uuid = uuid;
        this.powerUps = new ArrayList<>();
        this.powerInventory = new ItemStack[9];
    }

    public int getAttackDelay()
    {
        return attackDelay;
    }

    public void setAttackDelay(int attackDelay)
    {
        this.attackDelay = attackDelay;
    }

    public long getLastAttackTime()
    {
        return lastAttackTime;
    }

    public void setLastAttackTime(long lastAttackTime)
    {
        this.lastAttackTime = lastAttackTime;
    }

    public long getLastWOPRegenTime()
    {
        return lastWOPRegenTime;
    }

    public void setLastWOPRegenTime(long lastWOPRegenTime)
    {
        this.lastWOPRegenTime = lastWOPRegenTime;
    }

    public boolean isRegenHealth()
    {
        return isRegenHealth;
    }

    public void setRegenHealth(boolean regenHealth)
    {
        isRegenHealth = regenHealth;
    }

    public int getLevel()
    {
        return level;
    }

    public void setLevel(int level)
    {
        this.level = level;
    }

    public int getBaseATK()
    {
        return baseATK;
    }

    public void setBaseATK(int baseATK)
    {
        this.baseATK = baseATK;
    }

    public int getBaseDEF()
    {
        return baseDEF;
    }

    public void setBaseDEF(int baseDEF)
    {
        this.baseDEF = baseDEF;
    }

    public ItemStack getChargesArtifact()
    {
        return chargesArtifact;
    }

    public void setChargesArtifact(ItemStack chargesArtifact)
    {
        this.chargesArtifact = chargesArtifact;
    }

    public boolean hasChargesArtifact()
    {
        return this.chargesArtifact != null;
    }

    public int getChargesArtifactSlotOnRespawn()
    {
        return chargesArtifactSlotOnRespawn;
    }

    public void setChargesArtifactSlotOnRespawn(int chargesArtifactSlotOnRespawn)
    {
        this.chargesArtifactSlotOnRespawn = chargesArtifactSlotOnRespawn;
    }

    public UUID getUUID()
    {
        return uuid;
    }

    public void setUUID(UUID uuid)
    {
        this.uuid = uuid;
    }

    public void setPoisoned(boolean poisoned)
    {
        this.isPoisoned = poisoned;
    }

    public boolean isPoisoned()
    {
        return this.isPoisoned;
    }

    public void setPoisonTime(int poisonLevel, long time)
    {
        this.poisonTime[poisonLevel] = time;
    }

    public long getPoisonTime(int poisonLevel)
    {
        return this.poisonTime[poisonLevel];
    }

    public boolean inventoryIsSwapped(){return inventoryIsSwapped;}
    /* swaps to powerInventory */
    public void swapPowerInventory()
    {
        Player player = Bukkit.getPlayer(uuid);
        backupInventory = player.getInventory().getStorageContents();
        backupCursor = player.getInventory().getHeldItemSlot();
        updatePowerInventory();
        player.getInventory().setContents(powerInventory);
        player.getInventory().setHeldItemSlot(powerCursor);
        player.updateInventory();
        inventoryIsSwapped = true;
    }

    /* Swaps to the backed up inventory and cursor */
    public void swapMainInventory()
    {
        Player player = Bukkit.getPlayer(uuid);
        powerCursor = player.getInventory().getHeldItemSlot();
        player.getInventory().setContents(backupInventory);
        player.getInventory().setHeldItemSlot(backupCursor);
        player.updateInventory();
        inventoryIsSwapped = false;
    }

    /* Public method to changing powerInventory: scroll LEFT or RIGHT and updates powerInventory */
    public void scrollPowerInventory(Scroll direction)
    {
        switch(direction)
        {
            case LEFT:
                addPowerIndex(-7);
                break;
            case RIGHT:
                addPowerIndex(7);
                break;
        }

    }
    /* Updates the contents of the powerInventory based around the current powerCursor */
    private void updatePowerInventory()
    {
        int powerSize = powerUps.size();
        this.powerInventory = new ItemStack[9];
        if (powerSize > 0)
        {
            int invLimit = Math.min(powerSize, 7); // set the limit to the lower
            for (int i = 0; i < invLimit; i++)
            {
                int j = (powerIndex + i) % powerSize;
                powerInventory[i + 1] = powerUps.get(j);
            }
        }
        Player player = Bukkit.getPlayer(uuid);
        player.getInventory().setContents(powerInventory);
        player.updateInventory();
    }
    /* Changes the current powerIndex by adding the dIndex, result will modulus around the number of power ups */
    /* Finally, updates the powerInventory */
    private void addPowerIndex(int dIndex)
    {
        int powerSize = powerUps.size();
        powerIndex=(powerIndex+dIndex)%powerSize;
        if(powerIndex<0)
            powerIndex+=powerSize;
        updatePowerInventory();
    }

    public void addPowerUp(ItemStack itemStack)
    {
        for(ItemStack powerup : this.powerUps)
        {
            ItemMeta powerupItemMeta = powerup.getItemMeta();
            ItemMeta itemStackItemMeta = itemStack.getItemMeta();
            if(powerupItemMeta.getLocalizedName().equals(itemStackItemMeta.getLocalizedName()))
            {
                powerup.setAmount(powerup.getAmount() + itemStack.getAmount());
                if(this.inventoryIsSwapped)
                    this.updatePowerInventory();
                return;
            }
        }
        this.powerUps.add(itemStack);
        if(this.inventoryIsSwapped)
            this.updatePowerInventory();
    }

    public void removePowerUp(ItemStack itemStack)
    {
        for(ItemStack powerup : this.powerUps)
        {
            ItemMeta powerupItemMeta = powerup.getItemMeta();
            ItemMeta itemStackItemMeta = itemStack.getItemMeta();
            if(powerupItemMeta.getLocalizedName().equals(itemStackItemMeta.getLocalizedName()))
            {
                int amount = powerup.getAmount() - 1;
                System.out.println("PowerUp Amount:" + amount);
                if(amount < 1)
                    System.out.println("Powerup Removed: " + this.powerUps.remove(powerup));
                else
                    powerup.setAmount(amount);
                this.updatePowerInventory();
                return;
            }
        }
    }
}
