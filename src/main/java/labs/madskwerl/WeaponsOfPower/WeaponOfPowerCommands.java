package labs.madskwerl.WeaponsOfPower;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;


public class WeaponOfPowerCommands implements CommandExecutor
{
    public enum WOP_SYNTAX {WOP, ID, VALID, REMOVE}

    public WeaponOfPowerCommands(){}

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
    {
        Player player;
        if (sender instanceof Player)
        {
            System.out.println("Sender: " + ((Player) sender).getDisplayName());
            System.out.println("Command: " + command.toString());
            System.out.println("label: " + label);
            System.out.println("args: " + Arrays.deepToString(args));
            player = (Player) sender;

            //=================================== Start Block: No args ==========================================
            if (args.length == 0)
            {
                this.outputSyntax(WOP_SYNTAX.WOP, player);
            }
            //==================================== Start Block: Remove ==========================================
            else if (args[0].toLowerCase().equals("remove") || args[0].toLowerCase().equals("rm") || args[0].toLowerCase().equals("rem"))
            {
                try
                {
                    //==================================== Start Block: Remove Hand ====================================
                    if (args.length == 2 && (args[1].toLowerCase().equals("h") || args[1].toLowerCase().equals("hand")))
                    {
                        ItemStack item = player.getInventory().getItemInMainHand();
                        ItemMeta itemMeta = item.getItemMeta();
                        String localizedName = (itemMeta != null) ? itemMeta.getLocalizedName() : "";
                        if (WeaponOfPowerInventoryBank.isWop(localizedName)) //Remove item in hand/cursor if it is a WOP
                        {
                            item.setAmount(0);
                            WeaponOfPowerInventoryBank.WorldBank.removeWeaponOfPowerData(item);
                        }
                    }
                    //==================================== Start Block: Remove All ====================================
                    else if (args.length == 1) //Remove all weapons of power in player's inventory
                    {
                        for (ItemStack item : player.getInventory().getContents())
                        {
                            ItemMeta itemMeta = (item != null) ? item.getItemMeta() : null;
                            String localizedName = (itemMeta != null) ? itemMeta.getLocalizedName() : "";
                            if (WeaponOfPowerInventoryBank.isWop(localizedName))
                            {
                                WeaponOfPowerInventoryBank.WorldBank.removeWeaponOfPowerData(localizedName);
                                item.setAmount(0);
                            }
                        }
                    }
                    else
                        this.outputSyntax(WOP_SYNTAX.REMOVE, player);

                    player.updateInventory();
                }
                catch (Exception e)
                {
                    this.outputSyntax(WOP_SYNTAX.REMOVE, player);
                }
            }
            //================================ Start Block: Add WOP ======================================
            else
            {
                try
                {

                    int powerID;
                    if (args[0].matches("-?\\d+"))//if player sent the ID as a number
                        powerID = Integer.parseInt(args[0]);//use that number as the ID
                    else//otherwise, if the player sent a string name
                    {
                        args[0] = args[0].replace("_", " ");//format that name
                        powerID = labs.madskwerl.WeaponsOfPower.Powers.getID(args[0].toUpperCase());//and get the ID for that name
                    }

                    if(!Powers.getName(powerID, 1).equals(""))//if the ID matches a valid Power
                    {
                        if (args.length == 1)//if no power level specified
                        {
                            for(int i =  -10; i < 11; i +=2)//then give player a WOP for each level
                            {
                                ItemStack itemStack = new ItemStack(Material.IRON_SWORD, 1);
                                WeaponOfPowerData wopData = new WeaponOfPowerData(itemStack, powerID, i, player);
                                WeaponOfPowerInventoryBank.WorldBank.addWeaponOfPowerData(wopData);
                                player.getInventory().addItem(itemStack);
                            }
                            WeaponsOfPower.NSA.initPlayer(player);
                        }
                        else if (args.length == 2 && args[1].matches("-?\\d+"))//else if 2nd arg is a number
                        {
                            ItemStack itemStack = new ItemStack(Material.IRON_SWORD, 1);
                            WeaponOfPowerData wopData = new WeaponOfPowerData(itemStack, powerID, Integer.parseInt(args[1]), player);
                            WeaponOfPowerInventoryBank.WorldBank.addWeaponOfPowerData(wopData);
                            player.getInventory().addItem(itemStack);
                            WeaponsOfPower.NSA.initPlayer(player);
                        }
                        else
                            this.outputSyntax(WOP_SYNTAX.ID, player);//wrong amount of args, or 2nd arg is not a number
                    }
                    else
                        this.outputSyntax(WOP_SYNTAX.VALID, player);//name or ID was not a valid Power
                }catch (Exception e){return false;}
            }

        }
        return true;
    }


    private void outputSyntax(WOP_SYNTAX syntax, Player player)
    {
        switch(syntax)
        {
            case WOP:
                player.sendMessage("Usage: /wop [options]" +
                        "\n<power_id> <power_level>"       +
                        "\n<power_name> <power_level>"     +
                        "\nremove [hand]");
                break;
            case ID:
                player.sendMessage("Usage:\n/wop <power_id> <power_level>\n/wop <power_name> <power_level>");
                break;
            case VALID:
                player.sendMessage("Power Name or ID not found");
                player.sendMessage("Usage: /wop <power_name> <power_level>");
                break;
            case REMOVE:
                player.sendMessage("Usage:\n/wop remove [hand]\n/wop rm [h]");
                break;
            default:
                break;
        }
    }
}
