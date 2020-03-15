package labs.madskwerl.WeaponsOfPower;

import org.bukkit.scheduler.BukkitRunnable;

public class PoisonTimer extends BukkitRunnable
{
    private LivingEntityData led;

    public PoisonTimer(LivingEntityData led)
    {
        this.led = led;
    }

    @Override
    public void run()
    {
        WeaponsOfPower.NSA.applyPoison(led);
    }
}
