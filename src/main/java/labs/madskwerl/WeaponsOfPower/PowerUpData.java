package labs.madskwerl.WeaponsOfPower;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class PowerUpData
{
    private ItemStack itemStack = null; //holds the associated itemstack
    private ItemMeta itemMeta = null;
    private int hashCode = this.hashCode(); //used as a link in hashmaps

    public int powerID = -2;
    public int powerLevel = 0;

    public PowerUpData(ItemStack itemStack)
    {
        this.setItemStack(itemStack);
    }
    public void setItemStack(ItemStack itemStack)
    {
        this.itemStack = itemStack;
        if(itemStack !=null)
        {
            this.itemMeta = itemStack.getItemMeta();
            this.itemMeta.setLocalizedName(Integer.toString(this.hashCode));
            this.itemStack.setItemMeta(this.itemMeta);
        }
        else itemMeta = null;
    }


}
