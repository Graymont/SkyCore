package org.skycore.skycore;

import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

public class Database {
    String databasePath = "plugins/SkyCore/Database";
    String developmentPath = databasePath+"/Development/";
    String developmentDataFile = "DevelopmentData.yml";
    String developmentItemFile = "DevelopmentItem.yml";

    Events events;

    public HashMap<String, Double> priceList = new HashMap<>();
    public HashMap<String, Double> worthList = new HashMap<>();
    public HashMap<String, ItemStack> itemList = new HashMap<>();

    public HashMap<String, List<ItemStack>> shopItemList = new HashMap<>();

    public Database (Events e){
        events = e;
    }

    public ItemStack GetItem(String name){
        return itemList.get(name);
    }

    public Double GetPrice(String name){
        return priceList.get(name);
    }

    public Double GetWorth(String name){
        return worthList.get(name);
    }

    public void SetPrice(String name, double price){
        priceList.put(name, price);
        events.consoleLog(events.sendText("&bSaved Price: &6"+name+" &b(&eprice"+price+"&b)"));
    }

    public void SetWorth(String name, double price){
        worthList.put(name, price);
        events.consoleLog(events.sendText("&bSaved Worth: &6"+name+" &b(&eprice"+price+"&b)"));
    }

    public void SaveItem(String name, ItemStack item){
        itemList.put(name, item);
        events.consoleLog(events.sendText("&bSaved Items with name: &6"+name));
    }


    public void RemovePrice(String name){
        priceList.remove(name);
        events.consoleLog(events.sendText("&bRemoved Price: &6"+name));
    }

    public void RemoveWorth(String name){
        worthList.remove(name);
        events.consoleLog(events.sendText("&bRemoved Worth: &6"+name));
    }

    public void RemoveItem(String name){
        itemList.remove(name);
        events.consoleLog(events.sendText("&bRemoved Item with name: &6"+name));
    }

    void SavePrices(){
        File file = new File(developmentPath, developmentDataFile);
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        String parent = "prices.";
        for (String key : priceList.keySet()){
            config.set(parent+key, priceList.get(key));
        }
        try{
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void LoadPrices() {
        File file = new File(developmentPath, developmentDataFile);

        if (!file.exists()) {
            System.out.println("Prices file does not exist. No data loaded.");
            return;
        }

        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        String parent = "prices";
        String section = parent+".";

        if (config.isConfigurationSection(parent)) {
            Set<String> keys = config.getConfigurationSection(parent).getKeys(false);
            for (String key : keys) {
                priceList.put(key, config.getDouble(section + key));
            }
            System.out.println("Prices loaded successfully!");
        } else {
            System.out.println("No prices section found in file.");
        }
    }

    void SaveWorths(){
        File file = new File(developmentPath, developmentDataFile);
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        String parent = "worths.";
        for (String key : worthList.keySet()){
            config.set(parent+key, worthList.get(key));
        }
        try{
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void LoadWorths() {
        File file = new File(developmentPath, developmentDataFile);

        if (!file.exists()) {
            System.out.println("Worths file does not exist. No data loaded.");
            return;
        }

        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        String parent = "worths";
        String section = parent+".";

        if (config.isConfigurationSection(parent)) {
            Set<String> keys = config.getConfigurationSection(parent).getKeys(false);
            for (String key : keys) {
                worthList.put(key, config.getDouble(section + key));
            }
            System.out.println("Prices loaded successfully!");
        } else {
            System.out.println("No prices section found in file.");
        }
    }

    void LoadShopItems() {
        File file = new File(developmentPath, developmentDataFile);

        if (!file.exists()) {
            System.out.println("Shop Items file does not exist. No data loaded.");
            return;
        }

        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);

        String parent = "farm_items";
        if (config.isList(parent)) {

            List<String> keys = config.getStringList(parent);
            List<ItemStack> _iList = new ArrayList<>();
            for (String key : keys) {
                Material material = Material.getMaterial(key);
                if (material != null) {
                    _iList.add(new ItemStack(material));
                } else {
                    System.out.println("Invalid material: " + key);
                }
            }
            shopItemList.put(parent, _iList);
            System.out.println("Shop Items category " + parent + " loaded successfully!");
        } else {
            System.out.println("No category " + parent + " section found in file.");
        }

        parent = "mineral_items";
        if (config.isList(parent)) {

            List<String> keys = config.getStringList(parent);
            List<ItemStack> _iList = new ArrayList<>();
            for (String key : keys) {
                Material material = Material.getMaterial(key);
                if (material != null) {
                    _iList.add(new ItemStack(material));
                } else {
                    System.out.println("Invalid material: " + key);
                }
            }
            shopItemList.put(parent, _iList);
            System.out.println("Shop Items category " + parent + " loaded successfully!");
        } else {
            System.out.println("No category " + parent + " section found in file.");
        }

        parent = "mob_items";
        if (config.isList(parent)) {

            List<String> keys = config.getStringList(parent);
            List<ItemStack> _iList = new ArrayList<>();
            for (String key : keys) {
                Material material = Material.getMaterial(key);
                if (material != null) {
                    _iList.add(new ItemStack(material));
                } else {
                    System.out.println("Invalid material: " + key);
                }
            }
            shopItemList.put(parent, _iList);
            System.out.println("Shop Items category " + parent + " loaded successfully!");
        } else {
            System.out.println("No category " + parent + " section found in file.");
        }

        parent = "block_items";
        if (config.isList(parent)) {

            List<String> keys = config.getStringList(parent);
            List<ItemStack> _iList = new ArrayList<>();
            for (String key : keys) {
                Material material = Material.getMaterial(key);
                if (material != null) {
                    _iList.add(new ItemStack(material));
                } else {
                    System.out.println("Invalid material: " + key);
                }
            }
            shopItemList.put(parent, _iList);
            System.out.println("Shop Items category " + parent + " loaded successfully!");
        } else {
            System.out.println("No category " + parent + " section found in file.");
        }
    }



    // items

    void SaveItems(){
        File file = new File(developmentPath, developmentItemFile);
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        String parent = "items.";
        for (String key : itemList.keySet()){
            config.set(parent+key, itemList.get(key));
        }
        try{
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void LoadItems() {
        File file = new File(developmentPath, developmentItemFile);

        if (!file.exists()) {
            System.out.println("Items file does not exist. No data loaded.");
            return;
        }

        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        String parent = "items";
        String section = parent+".";

        if (config.isConfigurationSection(parent)) {
            Set<String> keys = config.getConfigurationSection(parent).getKeys(false);
            for (String key : keys) {
                itemList.put(key, config.getItemStack(section + key));
            }
            System.out.println("Items loaded successfully!");
        } else {
            System.out.println("No items section found in file.");
        }
    }


    public void LoadAllData(){
        LoadPrices();
        LoadWorths();
        LoadItems();
        LoadShopItems();
        events.consoleLog(events.sendText("&8[&6&lSkyCore Database&8] &bLoading all data..."));
    }

    public void SaveAllData(){
        SavePrices();
        SaveWorths();
        SaveItems();
        events.consoleLog(events.sendText("&8[&6&lSkyCore Database&8] &bSaving all data..."));
    }
}
