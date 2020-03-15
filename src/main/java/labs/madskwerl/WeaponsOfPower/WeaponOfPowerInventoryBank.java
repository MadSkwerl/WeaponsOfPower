package labs.madskwerl.WeaponsOfPower;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class WeaponOfPowerInventoryBank
{
    private Map<Integer, WeaponOfPowerData> map = new HashMap<>();
    static WeaponOfPowerInventoryBank WorldBank = new WeaponOfPowerInventoryBank();

    public Map<Integer, WeaponOfPowerData> getMap()
    {
        return this.map;
    }

    public void addWeaponOfPowerData(WeaponOfPowerData weaponOfPowerData)
    {
        this.map.put(weaponOfPowerData.getHashCode(), weaponOfPowerData);
    }

    public  void removeWeaponOfPowerData(int hashCode)
    {
        this.map.remove(hashCode);
    }

    public void removeWeaponOfPowerData(String hashcode)
    {
        try
        {
            this.map.remove(Integer.valueOf(hashcode));
        }
        catch(Exception e) {/*do nothing*/}
    }

    public  void removeWeaponOfPowerData(ItemStack itemStack)
    {
        this.removeWeaponOfPowerData(itemStack.getItemMeta().getLocalizedName());
    }

    public WeaponOfPowerData getWeaponOfPowerData(int hashCode)
    {
        return this.map.get(hashCode);
    }

    public WeaponOfPowerData getWeaponOfPowerData(String hashCode)
    {
        try
        {
            return this.map.get(Integer.valueOf(hashCode));
        }
        catch(Exception e)
        {
            return null;
        }
    }

    public boolean inInventory(String hashCode)
    {
        return this.getWeaponOfPowerData(hashCode) != null;
    }

    public boolean inInventory(int hashCode)
    {
        return this.getWeaponOfPowerData(hashCode) != null;
    }

    public boolean inInventory(ItemStack itemStack)
    {
        return this.getWeaponOfPowerData(itemStack.getItemMeta().getLocalizedName()) != null;
    }

    static boolean isWop(String hashcode)
    {
        return WeaponOfPowerInventoryBank.WorldBank.getWeaponOfPowerData(hashcode) != null;
    }

    static boolean isWop(int hashcode)
    {
        return WeaponOfPowerInventoryBank.WorldBank.getWeaponOfPowerData(hashcode) != null;
    }
}
