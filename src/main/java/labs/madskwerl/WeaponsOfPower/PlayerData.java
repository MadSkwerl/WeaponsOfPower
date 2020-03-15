package labs.madskwerl.WeaponsOfPower;


import java.util.UUID;

public class PlayerData extends LivingEntityData
{
    public PlayerData(UUID uuid)
    {
        super(uuid);
        this.setBaseATK(1);
        this.setBaseDEF(1);
        this.setAttackDelay(100);
    }

    public boolean hasWOPEquipped()
    {
        return this.currentWop != 0;
    }
}
