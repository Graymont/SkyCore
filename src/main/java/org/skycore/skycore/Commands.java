package org.skycore.skycore;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class Commands implements CommandExecutor, TabCompleter {
    public Events events;
    public Database database;

    public Commands(Events e, Database d){
        events = e;
        database = d;
    }

    public String sendText(String text) {
        return text.replaceAll("&", "§");
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {

        if (command.getName().equalsIgnoreCase("skycore")){

            if (sender instanceof Player){
                if (!sender.hasPermission("skycore.admin")){
                    sender.sendMessage(sendText("&4You dont't have permission!"));
                    return false;
                }
            }

            if (args[0].equalsIgnoreCase("openshop")){
                Player target = Bukkit.getPlayer(args[1]);
                assert target != null;
                events.OpenShop(target, false, "main");
            }

            else if (args[0].equalsIgnoreCase("opensell")){
                Player target = Bukkit.getPlayer(args[1]);
                assert target != null;
                events.OpenSell(target, false);
            }

            else if (args[0].equalsIgnoreCase("reload")){
                database.LoadAllData();
                sender.sendMessage(sendText("&aConfiguration Reloaded from yml!"));
            }

            else if (args[0].equalsIgnoreCase("save-all")){
                database.SaveAllData();
                database.LoadAllData();
                sender.sendMessage(sendText("&aConfiguration Saved to yml, and loaded to server!"));
            }

            else if (args[0].equalsIgnoreCase("setprice")){
                String name = args[1];
                double price = Double.parseDouble(args[2]);
                database.SetPrice(name, price);
                sender.sendMessage(sendText("&aSaved price: &2"+name+" &awith value &2"+price));
            }

            else if (args[0].equalsIgnoreCase("getprice")){
                String name = args[1];
                Double price = database.GetPrice(name);
                sender.sendMessage(sendText("&aPrice of: &2"+name+" &ais &2"+price));
            }

            else if (args[0].equalsIgnoreCase("removeprice")){
                String name = args[1];
                database.RemoveWorth(name);
                sender.sendMessage(sendText("&aRemoved price: &2"+name));
            }

            else if (args[0].equalsIgnoreCase("setworth")){
                String name = args[1];
                double worth = Double.parseDouble(args[2]);
                database.SetWorth(name, worth);
                sender.sendMessage(sendText("&aSaved worth: &2"+name+" &awith value &2"+worth));
            }

            else if (args[0].equalsIgnoreCase("getworth")){
                String name = args[1];
                Double price = database.GetWorth(name);
                sender.sendMessage(sendText("&aWorth of: &2"+name+" &ais &2"+price));
            }

            else if (args[0].equalsIgnoreCase("removeworth")){
                String name = args[1];
                database.RemoveWorth(name);
                sender.sendMessage(sendText("&aRemoved worth: &2"+name));
            }

            else if (args[0].equalsIgnoreCase("saveitem")){
                assert sender instanceof Player;
                Player player = (Player) sender;
                String name = args[1];
                ItemStack item = player.getInventory().getItemInMainHand();
                if (item.getType() == Material.AIR){
                    player.sendMessage(sendText("&4Cannot save air type!"));
                    return false;
                }
                database.SaveItem(name, item);
                sender.sendMessage(sendText("&aSaved item with name: &2"+name));
            }

            else if (args[0].equalsIgnoreCase("loaditem")){
                assert sender instanceof Player;
                Player player = (Player) sender;
                String name = args[1];
                ItemStack item = database.GetItem(name);
                if (item == null){
                    player.sendMessage(sendText("&4That item is not exist!"));
                    return false;
                }
                player.getInventory().addItem(item);
                sender.sendMessage(sendText("&aLoaded item with name: &2"+name));
            }

            else if (args[0].equalsIgnoreCase("removeitem")){
                assert sender instanceof Player;
                Player player = (Player) sender;
                String name = args[1];
                database.RemoveItem(name);
                sender.sendMessage(sendText("&aRemoved item named: &2"+name));
            }

            else if (args[0].equalsIgnoreCase("dailyroll")){
                events.InitMerchant();
                sender.sendMessage(sendText("&aDaily has been reset!"));
            }

            else if (args[0].equalsIgnoreCase("setfishheight")){
                events.fishHeight = Double.parseDouble(args[1]);
            }

            else if (args[0].equalsIgnoreCase("openmerchant")){

                Player player = Bukkit.getPlayer(args[1]);
                assert player != null;
                String type = args[2];
                events.OpenMerchant(player, type);
                events.consoleLog(sendText("&bOpening Merchant for &6"+player));

            }

            else if (args[0].equalsIgnoreCase("openmerchantmenu")){

                Player player = Bukkit.getPlayer(args[1]);
                assert player != null;
                events.OpenMerchantMenu(player);
                events.consoleLog(sendText("&bOpening Merchant Menu for &6"+player));

            }

            else if (args[0].equalsIgnoreCase("getspawnegg")){
                assert sender instanceof Player;
                Player player = (Player) sender;
                String mob = args[1];
                events.GetSpawnEgg(player, mob);
            }
        }

        else if (command.getName().equalsIgnoreCase("sellitem")){
            assert sender instanceof Player;
            Player player = (Player) sender;
            player.closeInventory();
            events.OpenSell((Player) sender, false);
        }

        else if (command.getName().equalsIgnoreCase("sellall")){
            sender.sendMessage(sendText("&aSelling all items in your inventory..."));
            events.SellAll((Player) sender);
        }

        else if (command.getName().equalsIgnoreCase("shop")){
            assert sender instanceof Player;
            Player player = (Player) sender;
            player.closeInventory();
            events.OpenShop((Player) sender, false, "main");
        }

        else if (command.getName().equalsIgnoreCase("buyamount")){
            assert sender instanceof Player;

            Player player = (Player) sender;
            int buyAmount = 1;
            if (args.length > 0){
                int argsValue = Integer.parseInt(args[0]);
                if (argsValue == 4 || argsValue == 16 || argsValue == 20
                        || argsValue == 24 || argsValue == 28 || argsValue == 32
                        || argsValue == 36|| argsValue == 40|| argsValue == 44
                        || argsValue == 48|| argsValue == 52|| argsValue == 56
                        || argsValue == 60|| argsValue == 64){
                    buyAmount = argsValue;
                }else{
                    player.sendMessage(sendText("&4Buy Amount must be multiple 4 (ex: 4,16,20,24,28,32,36,etc)"));
                }
                events.buyAmount.put(player, buyAmount);
                player.sendMessage(sendText("&aBuy Amount has been set to &b"+buyAmount));
                return false;
            }
            player.sendMessage(sendText("&aCorrect Usage: &b/buyamount <amount>"));
        }



        else if (command.getName().equalsIgnoreCase("catalog")){
            assert sender instanceof Player;
            Player player = (Player) sender;
            player.closeInventory();
            int page = 1;
            if (args.length > 0){
                page = Integer.parseInt(args[0]);
            }

            events.OpenCatalog((Player) sender, page);
            sender.sendMessage(sendText("&aOpening Catalog Page: &2"+page));
        }

        else if (command.getName().equalsIgnoreCase("worth")){
            Player player = (Player) sender;
            ItemStack item = player.getInventory().getItemInMainHand();
            if (item.getType() != Material.AIR){

                Double price = database.GetWorth(item.getType().toString().toLowerCase().replaceAll("_", "").trim());

                if (price == null){
                    player.sendMessage(sendText("&4This item is unsellable!"));
                    return false;
                }

                int itemAmount = item.getAmount();

                player.sendMessage(sendText("&b"+events.getFormattedName(item)+"&ais &2$"+price+" &aworth!"+" &6(total: &e$"+price*itemAmount+"&6)"));
            }
        }


        return false;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        List<String> suggestion = new ArrayList<>();

        if (command.getName().equalsIgnoreCase("skycore")){
            if (args.length == 1){
                suggestion.add("openshop");
                suggestion.add("opensell");
                suggestion.add("reload");
                suggestion.add("save-all");
                suggestion.add("setprice");
                suggestion.add("setworth");
                suggestion.add("saveitem");
                suggestion.add("getspawnegg");
                //suggestion.add("spawnfish");
                suggestion.add("getprice");
                suggestion.add("getworth");
                suggestion.add("loaditem");
                suggestion.add("removeprice");
                suggestion.add("removeworth");
                suggestion.add("removeitem");
                suggestion.add("openmerchant");
                suggestion.add("openmerchantmenu");
            }

            else if (args.length == 2){
                if (args[0].equalsIgnoreCase("openshop") ||
                        args[0].equalsIgnoreCase("openmerchant")
                        || args[0].equalsIgnoreCase("openmerchantmenu")){
                    for (Player player : Bukkit.getOnlinePlayers()){
                        suggestion.add(player.getName());
                    }
                }
            }

            else if (args.length == 3){
                if (args[0].equalsIgnoreCase("openshop")){
                    suggestion.add("farm");
                    suggestion.add("mineral");
                    //suggestion.add("reload");
                }
            }
        }

        else if (command.getName().equalsIgnoreCase("buyamount")){
            if (args.length == 1){
                suggestion.add("4");
                suggestion.add("16");
                suggestion.add("20");
                suggestion.add("24");
                suggestion.add("28");
                suggestion.add("32");
                suggestion.add("36");
                suggestion.add("40");
                suggestion.add("44");
                suggestion.add("48");
                suggestion.add("52");
                suggestion.add("56");
                suggestion.add("60");
                suggestion.add("64");
            }
        }

        return suggestion;
    }
}
