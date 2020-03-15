package labs.madskwerl.WeaponsOfPower;

import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class Delayed_BindChargesArtifact extends BukkitRunnable
{
    private Player player;
    public Delayed_BindChargesArtifact(Player player)
    {
        this.player = player;
    }

    @Override
    public void run()
    {
        WeaponsOfPower.NSA.bindChargesArtifact(this.player);
    }
}
