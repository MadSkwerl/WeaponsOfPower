package labs.madskwerl.WeaponsOfPower;

import org.bukkit.scheduler.BukkitRunnable;

public class Regen_Ammo extends BukkitRunnable
{
    private WeaponOfPowerData wopData;
    private LivingEntityData livingEntityData;
    public Regen_Ammo(WeaponOfPowerData wopData, LivingEntityData livingEntityData)
    {
        this.wopData = wopData;
        this.livingEntityData = livingEntityData;
    }
    @Override
    public void run()
    {
        WeaponsOfPower.NSA.regenAmmo(this.wopData, this.livingEntityData);
    }
}
