package org.skycore.skycore;

import com.bgsoftware.superiorskyblock.api.SuperiorSkyblockAPI;
import com.bgsoftware.superiorskyblock.api.events.IslandKickEvent;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import javax.naming.Name;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.bukkit.Bukkit.getServer;

public class Events implements Listener {
    public HashMap<Player, String> inventoryTitle = new HashMap<>();

    private final HashMap<UUID, Long> clickCooldowns = new HashMap<>();
    private final long COOLDOWN_TIME = 300; // Cooldown time in milliseconds (5000ms = 5 seconds)



    public String sendText(String text) {
        return text.replaceAll("&", "§");
    }

    public void sendClickableLink(Player player, String url, String message) {
        // Create the text component with the message text
        TextComponent textComponent = new TextComponent(message);

        // Set the click event to open a URL
        textComponent.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, url));

        // Send the message to the player
        player.spigot().sendMessage(textComponent);
    }

    public void executeConsoleCommand(String command) {
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
    }

    public void SendActionBar(Player p, String m) {
        p.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(m));
    }

    public void PlaySound(Sound s, Entity e, float volume, float pitch) {
        e.getWorld().playSound(e.getLocation(), s, volume, pitch);
    }

    public void PlaySoundAt(Sound sound, Location location, float volume, float pitch) {
        if (location.getWorld() != null) {
            location.getWorld().playSound(location, sound, volume, pitch);
        }
    }

    public Inventory OpenGUI(Player p, int size, String name) {
        Inventory gui = Bukkit.createInventory(new CustomInventoryHolder(null), size*9, name);

        return gui;
    }

    @EventHandler
    public void onInventoryClickHolder(InventoryClickEvent event) {
        Inventory clickedInventory = event.getClickedInventory();

        if (clickedInventory == null) return;

        InventoryHolder holder = clickedInventory.getHolder();
        if (holder instanceof CustomInventoryHolder) {
            event.setCancelled(true);
        }
    }

    public void consoleLog(String message) {
        ConsoleCommandSender console = getServer().getConsoleSender();
        console.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
    }

    public String checkName(String text){
        return text.replaceAll("§.", "").trim().replaceAll("'s", "").trim();
    }

    public String uncolouredText(String text) {
        return text.replaceAll("§.|[^\\x00-\\x7F]|\\d+|[^a-zA-Z_ ]", "").trim();
    }

    public String numberInText(String text) {
        String cleaned = text.replaceAll("§.|[^\\x00-\\x7F]", "").trim();
        // Replace text
        String pattern = "\\d+";
        Pattern compiledPattern = Pattern.compile(pattern);
        Matcher matcher = compiledPattern.matcher(cleaned);
        StringBuilder result = new StringBuilder();
        while (matcher.find()) {
            // Append matched substring to result
            result.append(matcher.group());
        }
        return result.toString().trim();
    }
    public static Economy econ = null;

    public void setupEconomy() {
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        econ = rsp.getProvider();
    }

    public Economy getEconomy() {
        return econ;
    }

    public Double getPlayerBalance(Player player){
        return getEconomy().getBalance(player);
    }

    public void addPlayerBalance(Player player, double amount){
        getEconomy().depositPlayer(player, amount);

        if (amount > 0){
            player.sendMessage(sendText(" "));
            player.sendMessage(sendText("&a+&f$"+amount));
        }
    }

    public void removePlayerBalance(Player player, double amount){
        getEconomy().withdrawPlayer(player, amount);
        player.sendMessage(sendText(" "));
        player.sendMessage(sendText("&c-&f$"+amount));
    }

    /*@EventHandler
    public void DisableBugClick(InventoryClickEvent event) {
        if (event.getInventory().getType() == InventoryType.CHEST){
            Player player = (Player) event.getWhoClicked();
            if (!uncolouredText(player.getOpenInventory().getTitle()).equals("Chest")){
                ItemStack[] itemList = player.getOpenInventory().getTopInventory().getContents();

                for (ItemStack item : itemList){
                    if (item != null && item.getType() == Material.BLACK_STAINED_GLASS_PANE){
                        event.setCancelled(true);
                        //player.sendMessage(sendText("&aHas Black Stained Glass Pane!"));
                        break;
                    }
                }
            }
        }
    }*/

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getWhoClicked() instanceof Player) {
            Player player = (Player) event.getWhoClicked();
            if (!inventoryTitle.get(player).equals("none")){
                event.setCancelled(true);
                if (!isAllowedClick(event.getClick())){
                    event.setCancelled(true);
                    player.closeInventory();
                    return;
                }

                UUID playerId = player.getUniqueId();
                long currentTime = System.currentTimeMillis();

                if (clickCooldowns.containsKey(playerId)) {
                    long lastClickTime = clickCooldowns.get(playerId);

                    if ((currentTime - lastClickTime) < COOLDOWN_TIME) {
                        //player.sendMessage("You must wait before clicking again.");
                        event.setCancelled(true);
                        player.closeInventory();
                        return;
                    }
                }
                clickCooldowns.put(playerId, currentTime);
            }else{
                if (player.getInventory().getType() == InventoryType.CHEST){
                    if (uncolouredText(player.getOpenInventory().getTitle()).contains("Chest")){
                        event.setCancelled(true);
                        if (!isAllowedClick(event.getClick())){
                            event.setCancelled(true);
                            player.closeInventory();
                        }
                    }
                }
            }
        }
    }

    public boolean isAllowedClick(ClickType type){
        if (type == ClickType.SHIFT_RIGHT || type == ClickType.DROP ||
                type == ClickType.CONTROL_DROP || type == ClickType.SHIFT_LEFT ||
                type == ClickType.SWAP_OFFHAND || type == ClickType.DOUBLE_CLICK ||
                type == ClickType.MIDDLE || type == ClickType.WINDOW_BORDER_LEFT ||
                type == ClickType.WINDOW_BORDER_RIGHT || type == ClickType.UNKNOWN){
            return false;
        }else{
            return true;
        }
    }

    public HashMap<Player, Integer> buyAmount = new HashMap<>();

    public void OpenShop(Player player, boolean openAgain, String type){
        SkyCore plugin = SkyCore.getPlugin(SkyCore.class);
        if (player.getVehicle() != null){
            return;
        }

        player.setMetadata("gui", new FixedMetadataValue(plugin, true));
        inventoryTitle.put(player, "shop");
        buyAmount.putIfAbsent(player, 1);

        int size = 6;

        if (type.contains("main")){
            size = 3;
        }

        Inventory inv = OpenGUI(player, size, "Shop");

        if (!openAgain){
            player.openInventory(inv);
            consoleLog(sendText("&bOpening Shop Menu for &6"+player.getName()));
        }

        ItemStack border = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta borderMeta = border.getItemMeta();
        borderMeta.setDisplayName(" ");
        border.setItemMeta(borderMeta);

        shopType.put(player, type);

        for (int i = 0; i < inv.getSize(); i++) {
            player.getOpenInventory().setItem(i, new ItemStack(Material.AIR));
        }

        if (type.equals("main")){
            for (int i = 0; i < 9; i++) {
                player.getOpenInventory().setItem(i, border);
            }
            for (int i = 18; i < 27; i++) {
                player.getOpenInventory().setItem(i, border);
            }
            player.getOpenInventory().setItem(10, getMainShopIcon("farms-icon"));
            player.getOpenInventory().setItem(12, getMainShopIcon("minerals-icon"));
            player.getOpenInventory().setItem(14, getMainShopIcon("mobs-icon"));
            player.getOpenInventory().setItem(16, getMainShopIcon("blocks-icon"));
            player.getOpenInventory().setItem(22, getMainShopIcon("spawners-icon"));
        }
        else if (type.equals("farms")){
            List<ItemStack> items = plugin.database.shopItemList.get("farm_items");

            for (int i = 0; i < items.size(); i++) {
                player.getOpenInventory().setItem(i, getShopVanillaItem(items.get(i), player));
            }
        }
        else if (type.equals("minerals")){
            List<ItemStack> items = plugin.database.shopItemList.get("mineral_items");

            for (int i = 0; i < items.size(); i++) {
                player.getOpenInventory().setItem(i, getShopVanillaItem(items.get(i), player));
            }
        }
        else if (type.equals("mobs")){
            List<ItemStack> items = plugin.database.shopItemList.get("mob_items");
            int index = 0;
            for (int i = 0; i < items.size(); i++) {
                player.getOpenInventory().setItem(i, getShopVanillaItem(items.get(i), player));
                index++;
            }
            List<String> mobEggList = new ArrayList<>();
            mobEggList.add("Wolf");
            mobEggList.add("Camel");
            mobEggList.add("Horse");
            mobEggList.add("Cat");
            mobEggList.add("Allay");
            mobEggList.add("Villager");
            int mobIndex = 0;
            for (int i = 0; i < mobEggList.size(); i++) {
                player.getOpenInventory().setItem(index, getEggShop(mobEggList.get(mobIndex)));
                mobIndex++;
                index++;
            }
        }
        else if (type.equals("blocks")){
            List<ItemStack> items = plugin.database.shopItemList.get("block_items");

            for (int i = 0; i < items.size(); i++) {
                player.getOpenInventory().setItem(i, getShopVanillaItem(items.get(i), player));
            }
        }

        else if (type.equals("spawners")){

            player.getOpenInventory().setItem(0, getSpawnerShop(EntityType.PIG));
            player.getOpenInventory().setItem(1, getSpawnerShop(EntityType.CHICKEN));
            player.getOpenInventory().setItem(2, getSpawnerShop(EntityType.SHEEP));
            player.getOpenInventory().setItem(3, getSpawnerShop(EntityType.COW));
            player.getOpenInventory().setItem(4, getSpawnerShop(EntityType.RABBIT));

            player.getOpenInventory().setItem(9, getSpawnerShop(EntityType.ZOMBIE));
            player.getOpenInventory().setItem(10, getSpawnerShop(EntityType.SKELETON));
            player.getOpenInventory().setItem(11, getSpawnerShop(EntityType.SPIDER));
            player.getOpenInventory().setItem(12, getSpawnerShop(EntityType.CREEPER));
            player.getOpenInventory().setItem(13, getSpawnerShop(EntityType.SLIME));

            player.getOpenInventory().setItem(18, getSpawnerShop(EntityType.BLAZE));
            player.getOpenInventory().setItem(19, getSpawnerShop(EntityType.GUARDIAN));
            player.getOpenInventory().setItem(20, getSpawnerShop(EntityType.IRON_GOLEM));
            player.getOpenInventory().setItem(21, getSpawnerShop(EntityType.PIGLIN));
            player.getOpenInventory().setItem(22, getSpawnerShop(EntityType.WARDEN));

        }

        if (!type.contains("main")){
            for (int i = 45; i < 54; i++) {
                player.getOpenInventory().setItem(i, border);
            }
            player.getOpenInventory().setItem(50, getShopBuyAmountModifier("add"));
            player.getOpenInventory().setItem(48, getShopBuyAmountModifier("remove"));
            player.getOpenInventory().setItem(49, getShopBuyAmount(player));
            player.getOpenInventory().setItem(45, getMainShopIcon("main-icon"));
        }
    }

    public ItemStack getSpawnerShop(EntityType type){

        SkyCore plugin = SkyCore.getPlugin(SkyCore.class);

        ItemStack item = new ItemStack(Material.SPAWNER);
        BlockStateMeta blockMeta = (BlockStateMeta) item.getItemMeta();
        CreatureSpawner spawner = (CreatureSpawner) blockMeta.getBlockState();
        spawner.setSpawnedType(type);
        blockMeta.setBlockState(spawner);

        item.setItemMeta(blockMeta);

        ItemMeta meta = item.getItemMeta();
        NamespacedKey priceKey = new NamespacedKey(plugin, "item-price");

        Double price = plugin.database.GetPrice(
                type.toString().toLowerCase().replaceAll("_", "").trim()+item.getType().toString().toLowerCase().replaceAll("_", "").trim());
        if (price == null){
            price = 1000000.0;
        }
        meta.getPersistentDataContainer().set(priceKey, PersistentDataType.DOUBLE, price);
        meta.setDisplayName(sendText("&aMob Spawner"));

        List<String> itemLore = new ArrayList<>();

        itemLore.add(sendText(""));
        itemLore.add(sendText("&7Price: &f"+price*item.getAmount()));
        itemLore.add(sendText(""));
        if (type.toString().equals("BLAZE")){
            itemLore.add(sendText("&9Drops:"));
            itemLore.add(sendText("&8- &a100% &f(13-2) &7Blaze Rod"));
        }
        else if (type.toString().equals("GUARDIAN")){
            itemLore.add(sendText("&9Drops:"));
            itemLore.add(sendText("&8- &a100% &f(14-3) &7Prismarine Shard"));
            itemLore.add(sendText("&8- &e50% &7Prismarine Crystals"));
        }
        else if (type.toString().equals("IRON_GOLEM")){
            itemLore.add(sendText("&9Drops:"));
            itemLore.add(sendText("&8- &a100% &f(15-4) &7Iron Ingot"));
        }
        else if (type.toString().equals("PIGLIN")){
            itemLore.add(sendText("&9Drops:"));
            itemLore.add(sendText("&8- &a100% &f(20-5) &7Gold Ingot"));
        }
        else if (type.toString().equals("WARDEN")){
            itemLore.add(sendText("&9Drops:"));
            itemLore.add(sendText("&8- &a100% &7Sculk Catalyst"));
        }


        else if (type.toString().equals("CHICKEN")){
            itemLore.add(sendText("&9Drops:"));
            itemLore.add(sendText("&8- &a100% &f(1-2) &7Raw Chicken"));
            itemLore.add(sendText("&8- &e50% &7Feather"));
        }
        else if (type.toString().equals("PIG")){
            itemLore.add(sendText("&9Drops:"));
            itemLore.add(sendText("&8- &a100% &f(1-3) &7Porkchop"));
        }
        else if (type.toString().equals("SHEEP")){
            itemLore.add(sendText("&9Drops:"));
            itemLore.add(sendText("&8- &a100% &f(1-4) &7Mutton"));
            itemLore.add(sendText("&8- &e50% &7White Wool"));
        }
        else if (type.toString().equals("COW")){
            itemLore.add(sendText("&9Drops:"));
            itemLore.add(sendText("&8- &a100% &f(1-5) &7Beef"));
            itemLore.add(sendText("&8- &e50% &7Leather"));
        }
        else if (type.toString().equals("RABBIT")){
            itemLore.add(sendText("&9Drops:"));
            itemLore.add(sendText("&8- &a100% &f(1-6) &7Raw Rabbit"));
            itemLore.add(sendText("&8- &e50% &7Rabbit Hide"));
            itemLore.add(sendText("&8- &e50% &7Rabbit's Foot"));
        }

        else if (type.toString().equals("ZOMBIE")){
            itemLore.add(sendText("&9Drops:"));
            itemLore.add(sendText("&8- &a100% &f(1-7) &7Rotten Flesh"));
        }
        else if (type.toString().equals("SKELETON")){
            itemLore.add(sendText("&9Drops:"));
            itemLore.add(sendText("&8- &a100% &f(1-8) &7Bone"));
            itemLore.add(sendText("&8- &e50% &7Arrow"));
        }
        else if (type.toString().equals("SPIDER")){
            itemLore.add(sendText("&9Drops:"));
            itemLore.add(sendText("&8- &a100% &f(1-9) &7String"));
            itemLore.add(sendText("&8- &e50% &7Spider Eye"));
        }
        else if (type.toString().equals("CREEPER")){
            itemLore.add(sendText("&9Drops:"));
            itemLore.add(sendText("&8- &a100% &f(1-10) &7Gunpowder"));
        }
        else if (type.toString().equals("ENDERMAN")){
            itemLore.add(sendText("&9Drops:"));
            itemLore.add(sendText("&8- &a100% &f(1-11) &7Ender Pearl"));
            itemLore.add(sendText("&8- &e50% &7Ender Eye"));
        }
        else if (type.toString().equals("SLIME")){
            itemLore.add(sendText("&9Drops:"));
            itemLore.add(sendText("&8- &a100% &f(1-12) &7Slime Ball"));
        }
        itemLore.add(sendText(""));
        itemLore.add(sendText("&aClick to buy"));

        meta.setLore(itemLore);

        item.setItemMeta(meta);

        return item;
    }

    public ItemStack getEggShop(String type){

        SkyCore plugin = SkyCore.getPlugin(SkyCore.class);

        ItemStack item = new ItemStack(Material.EGG);

        ItemMeta meta = item.getItemMeta();

        meta.addEnchant(Enchantment.UNBREAKING, 1, true);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);

        NamespacedKey priceKey = new NamespacedKey(plugin, "item-price");

        Double price = plugin.database.GetPrice(
                type.toLowerCase().replaceAll("_", "").trim()+"spawnegg");
        if (price == null){
            price = 1000000.0;
        }
        meta.getPersistentDataContainer().set(priceKey, PersistentDataType.DOUBLE, price);
        meta.setDisplayName(sendText("&aSpawn Egg"));

        List<String> itemLore = new ArrayList<>();
        itemLore.add(sendText("&7Type: &f"+type));
        itemLore.add(sendText(""));
        itemLore.add(sendText("&7Price: &f"+price*item.getAmount()));
        itemLore.add(sendText(""));
        itemLore.add(sendText("&aClick to buy"));

        meta.setLore(itemLore);
        item.setItemMeta(meta);

        return item;
    }

    @EventHandler
    public void OnSpawnerPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItemInHand();
        if (item.getType() == Material.SPAWNER) {
            Block block = event.getBlockPlaced();
            if (block.getType() == Material.SPAWNER) {
                BlockStateMeta meta = (BlockStateMeta) item.getItemMeta();
                if (meta != null) {
                    CreatureSpawner itemSpawner = (CreatureSpawner) meta.getBlockState();
                    EntityType type = itemSpawner.getSpawnedType();

                    CreatureSpawner blockSpawner = (CreatureSpawner) block.getState();
                    blockSpawner.setSpawnedType(type);
                    blockSpawner.update();
                }
            }
        }
    }


    public ItemStack getMainShopIcon(String name){

        SkyCore plugin = SkyCore.getPlugin(SkyCore.class);
        ItemStack item = new ItemStack(Material.STICK);
        ItemMeta meta = item.getItemMeta();

        NamespacedKey key = new NamespacedKey(plugin, "type");

        if (name.equals("farms-icon")){
            item.setType(Material.WHEAT);
            meta.setDisplayName(sendText("&bFarms"));
        }

        else if (name.equals("minerals-icon")){
            item.setType(Material.IRON_INGOT);
            meta.setDisplayName(sendText("&bMinerals"));
        }

        else if (name.equals("mobs-icon")){
            item.setType(Material.CHICKEN_SPAWN_EGG);
            meta.setDisplayName(sendText("&bMobs"));
        }
        else if (name.equals("spawners-icon")){
            item.setType(Material.SPAWNER);
            meta.setDisplayName(sendText("&bSpawners"));
        }
        else if (name.equals("blocks-icon")){
            item.setType(Material.GRASS_BLOCK);
            meta.setDisplayName(sendText("&bBlocks"));
        }

        else if (name.equals("main-icon")){
            item.setType(Material.CHEST);
            meta.setDisplayName(sendText("&cBack"));
        }
        meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, name.replaceAll("-icon", "").trim());

        item.setItemMeta(meta);

        return item;
    }

    public void OpenSell(Player player, boolean openAgain){
        if (player.getVehicle() != null){
            return;
        }

        inventoryTitle.put(player, "sellitem");
        buyAmount.putIfAbsent(player, 1);

        Inventory inv = OpenGUI(player, 3, "Sell Item");

        if (!openAgain){
            player.openInventory(inv);
            consoleLog(sendText("&bOpening Sell Menu for &6"+player.getName()));
        }

        ItemStack border = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta borderMeta = border.getItemMeta();
        borderMeta.setDisplayName(" ");
        border.setItemMeta(borderMeta);

        for (int i = 0; i < 9; i++) {
            player.getOpenInventory().setItem(i, border);
        }

        for (int i = 18; i < 27; i++) {
            player.getOpenInventory().setItem(i, border);
        }

        player.getOpenInventory().setItem(13, getGuiItem("sellitem"));
    }

    @EventHandler
    public void OnPlayerJoin(PlayerJoinEvent event){
        Player player = event.getPlayer();
        inventoryTitle.putIfAbsent(player, "none");
    }

    public ItemStack getGuiItem(String name){
        ItemStack item = new ItemStack(Material.STICK);
        ItemMeta meta = item.getItemMeta();

        List<String> itemLore = new ArrayList<>();
        if (name.equals("sellitem")){
            meta.setDisplayName(sendText("&bHow to Sell Item?"));
            itemLore.add(sendText(" "));
            itemLore.add(sendText("&7Click at Item that you wanted"));
            itemLore.add(sendText("&7to sell inside your Inventory"));
            itemLore.add(sendText(" "));
        }

        meta.setLore(itemLore);
        item.setItemMeta(meta);

        return item;
    }

    public ItemStack getShopBuyAmount(Player player) {
        SkyCore plugin = SkyCore.getPlugin(SkyCore.class);
        ItemStack item = new ItemStack(Material.PAPER);
        ItemMeta meta = item.getItemMeta();

        NamespacedKey buyIconKey = new NamespacedKey(plugin, "buyicon");
        meta.getPersistentDataContainer().set(buyIconKey, PersistentDataType.BOOLEAN, true);

        meta.setDisplayName(sendText("&7Buy Amount: &f&l"+buyAmount.get(player)));

        item.setItemMeta(meta);

        return item;
    }

    public ItemStack getShopBuyAmountModifier(String name){
        SkyCore plugin = SkyCore.getPlugin(SkyCore.class);
        ItemStack item = new ItemStack(Material.ARROW);
        ItemMeta meta = item.getItemMeta();

        if (name.equals("add")){
            NamespacedKey key = new NamespacedKey(plugin, "add");
            meta.getPersistentDataContainer().set(key, PersistentDataType.BOOLEAN, true);
            meta.setDisplayName(sendText("&8[&a+&8] &7Add buy amount"));
        }
        else if (name.equals("remove")){
            NamespacedKey key = new NamespacedKey(plugin, "remove");
            meta.getPersistentDataContainer().set(key, PersistentDataType.BOOLEAN, true);
            meta.setDisplayName(sendText("&8[&c-&8] &7Remove buy amount"));
        }

        item.setItemMeta(meta);

        return item;
    }

    public ItemStack getShopVanillaItem(ItemStack item, Player player){
        SkyCore plugin = SkyCore.getPlugin(SkyCore.class);
        ItemMeta meta = item.getItemMeta();

        String itemKey = item.getType().toString().toLowerCase().replaceAll("_", "").trim();
        Double price = plugin.database.GetPrice(itemKey);

        if (price == null){
            price = 1000000.0;
        }

        NamespacedKey priceKey = new NamespacedKey(plugin, "item-price");
        meta.getPersistentDataContainer().set(priceKey, PersistentDataType.DOUBLE, price);
        Double itemWorth = plugin.database.GetWorth(item.getType().toString().toLowerCase().replaceAll("_", "").trim());
        if (itemWorth == null){
            itemWorth = 0.0;
        }

        meta.setDisplayName(sendText("&f"+getFormattedName(item)));
        List<String> itemLore = new ArrayList<>();
        itemLore.add(sendText(" "));
        itemLore.add(sendText("&7Buy Price: &f"+price*buyAmount.get(player)));
        if (itemWorth > 0){
            itemLore.add(sendText("&7Sell Value: &f"+itemWorth));
            itemLore.add(sendText(" "));
            itemLore.add(sendText("&e[!] &f/sellitem &7to sell this item"));
        }
        itemLore.add(sendText(" "));
        itemLore.add(sendText("&aClick to buy"));

        meta.setLore(itemLore);
        item.setItemMeta(meta);
        item.setAmount(buyAmount.get(player));

        return item;
    }

    public HashMap<Player, String> shopType = new HashMap<>();

    @EventHandler
    public void OnInventoryClick(InventoryClickEvent event){
        Player player = (Player) event.getWhoClicked();
        ItemStack item = event.getCurrentItem();

        inventoryTitle.putIfAbsent(player, "none");

        if (inventoryTitle.get(player).equals("none")){
            return;
        }

        event.setCancelled(true);
        if (inventoryTitle.get(player).equals("shop")){
            SkyCore plugin = SkyCore.getPlugin(SkyCore.class);
            NamespacedKey priceKey = new NamespacedKey(plugin, "item-price");
            NamespacedKey addKey = new NamespacedKey(plugin, "add");
            NamespacedKey removeKey = new NamespacedKey(plugin, "remove");
            NamespacedKey categoryKey = new NamespacedKey(plugin, "type");
            buyAmount.putIfAbsent(player, 1);
            if (item != null && item.getType() != Material.AIR){
                ItemMeta meta = item.getItemMeta();

                PersistentDataContainer container = meta.getPersistentDataContainer();
                if (container.has(priceKey, PersistentDataType.DOUBLE)){
                    Double itemPrice = meta.getPersistentDataContainer().get(priceKey, PersistentDataType.DOUBLE);
                    int itemAmount = item.getAmount();
                    double price = itemPrice*itemAmount;

                    Double balance = getPlayerBalance(player);

                    if (balance >= price){
                        String actualDisplayname = meta.getDisplayName();
                        if (item.getType() != Material.SPAWNER && !uncolouredText(meta.getDisplayName()).contains("Spawn Egg")){
                            meta.setDisplayName(null);
                        }
                        if (!uncolouredText(meta.getDisplayName()).contains("Spawn Egg")){
                            meta.setLore(null);
                        }else{
                            List<String> storedLore = new ArrayList<>();
                            for (String lore : meta.getLore()){
                                if (lore != null){
                                    if (uncolouredText(lore).contains("Type")){
                                        storedLore.add(lore);
                                    }
                                }
                            }
                            meta.setLore(storedLore);
                        }
                        meta.getPersistentDataContainer().remove(priceKey);
                        item.setItemMeta(meta);
                        Map<Integer, ItemStack> addedItem = player.getInventory().addItem(item);
                        if (addedItem.isEmpty()){
                            removePlayerBalance(player, price);
                            PlaySound(Sound.BLOCK_NOTE_BLOCK_CHIME, player, 1, 1);
                            player.sendMessage(sendText("&aSuccessfully bought &bx"+itemAmount+" "+actualDisplayname+"&afor &2$"+price));
                        }else{
                            int successfullyAdded = itemAmount - addedItem.values().stream().mapToInt(ItemStack::getAmount).sum();

                            if (successfullyAdded == 0) {
                                player.sendMessage(sendText("&4Your inventory is full!"));
                            } else {
                                double partialPrice = successfullyAdded * itemPrice;
                                removePlayerBalance(player, partialPrice);
                                PlaySound(Sound.BLOCK_NOTE_BLOCK_CHIME, player, 1, 1);
                                player.sendMessage(sendText("&aSuccessfully bought &bx" + successfullyAdded + " " + actualDisplayname + "&afor &2$" + partialPrice));

                                int remainingItems = itemAmount - successfullyAdded;
                                player.sendMessage(sendText("&cYour inventory was too full to add &bx" + remainingItems + " " + actualDisplayname + "&c."));
                            }
                        }
                        OpenShop(player, true, shopType.get(player));
                    }else{
                        player.sendMessage(sendText("&4You don't have enough money!"));
                    }
                }

                else if (container.has(addKey, PersistentDataType.BOOLEAN)){
                    int currentBuyAmount = buyAmount.get(player);
                    if (currentBuyAmount < 64){
                        if (currentBuyAmount == 1){
                            currentBuyAmount += 3;
                        }else{
                            currentBuyAmount += 4;
                        }
                    }
                    buyAmount.put(player, currentBuyAmount);
                    OpenShop(player, true, shopType.get(player));
                }

                else if (container.has(removeKey, PersistentDataType.BOOLEAN)){
                    int currentBuyAmount = buyAmount.get(player);
                    if (currentBuyAmount >= 4){
                        currentBuyAmount -= 4;
                    }
                    if (currentBuyAmount <= 0){
                        currentBuyAmount = 1;
                    }
                    buyAmount.put(player, currentBuyAmount);
                    OpenShop(player, true, shopType.get(player));
                }

                else if (container.has(categoryKey, PersistentDataType.STRING)){
                    String shop_type = meta.getPersistentDataContainer().get(categoryKey, PersistentDataType.STRING);
                    shopType.put(player, shop_type);
                    player.closeInventory();
                    OpenShop(player, false, shopType.get(player));
                }
            }
        }

        else if (inventoryTitle.get(player).equals("sellitem")) {
            SkyCore plugin = SkyCore.getPlugin(SkyCore.class);
            if (item != null && item.getType() != Material.AIR) {
                Inventory clickedInventory = event.getClickedInventory();
                InventoryView view = event.getView();

                if (clickedInventory != null && clickedInventory.equals(view.getBottomInventory())) {
                    ItemMeta meta = item.getItemMeta();
                    String itemType = item.getType().toString().toLowerCase().replaceAll("_", "").trim();
                    Double price = plugin.database.GetWorth(itemType);
                    int itemAmount = item.getAmount();
                    if (price == null){
                        player.sendMessage(sendText("&4You can't sell this item!"));
                        return;
                    }

                    event.setCurrentItem(new ItemStack(Material.AIR));
                    addPlayerBalance(player, price*itemAmount);
                    player.sendMessage(sendText("&aSuccessfully sold &bx"+itemAmount+" "+getFormattedName(item)+"&afor &2$"+price*itemAmount));
                    PlaySound(Sound.ENTITY_EXPERIENCE_ORB_PICKUP, player, 3, 1);
                    OpenSell(player, true);
                }
            }
        }
        else if (inventoryTitle.get(player).equals("merchant")) {
            SkyCore plugin = SkyCore.getPlugin(SkyCore.class);
            if (item != null && item.getType() != Material.AIR) {
                ItemMeta meta = item.getItemMeta();
                NamespacedKey key = new NamespacedKey(plugin, "merchant-category");
                if (meta.getPersistentDataContainer().has(key, PersistentDataType.STRING)){
                    player.closeInventory();
                    OpenMerchant(player, meta.getPersistentDataContainer().get(key, PersistentDataType.STRING));
                }
            }
        }
    }

    private boolean hasEnoughSpace(Player player, ItemStack item, int amount) {
        int totalSpace = 0;

        for (ItemStack content : player.getInventory().getContents()) {
            if (content == null || content.getType() == Material.AIR) {
                // Empty slot can hold the full stack
                totalSpace += item.getMaxStackSize();
            } else if (content.getType() == item.getType()) {
                // Partially filled stack of the same item
                totalSpace += (item.getMaxStackSize() - content.getAmount());
            }
        }

        return totalSpace >= amount;
    }

    public StringBuilder getFormattedName(ItemStack item){
        String[] words = item.getType().toString().toLowerCase().split("_");
        StringBuilder formattedName = new StringBuilder();
        for (String word : words) {
            formattedName.append(word.substring(0, 1).toUpperCase()).append(word.substring(1)).append(" ");
        }
        return formattedName;
    }

    public StringBuilder getFormattedName2(EntityType type){
        String[] words = type.toString().toLowerCase().split("_");
        StringBuilder formattedName = new StringBuilder();
        for (String word : words) {
            formattedName.append(word.substring(0, 1).toUpperCase()).append(word.substring(1)).append(" ");
        }
        return formattedName;
    }

    @EventHandler
    public void OnInventoryClose(InventoryCloseEvent event){
        Player player = (Player) event.getPlayer();
        inventoryTitle.put(player, "none");
        //buyAmount.put(player, 1);
    }

    public void SellAll(Player player){
        if (player.getVehicle() != null){
            player.sendMessage(sendText("&4Dismount before selling items!"));
            return;
        }

        SkyCore plugin = SkyCore.getPlugin(SkyCore.class);

        PlayerInventory inventory = player.getInventory();

        for (int i = 0; i < 36; i++) {
            ItemStack item = inventory.getItem(i);
            if (item != null && item.getType() != Material.AIR){
                Double worth = plugin.database.GetWorth(item.getType().toString().toLowerCase().replaceAll("_", "").trim());
                int itemAmount = item.getAmount();
                if (worth != null){
                    player.getInventory().setItem(i, new ItemStack(Material.AIR));
                    addPlayerBalance(player, worth*itemAmount);
                    player.sendMessage(sendText("&aSuccessfully sold &bx"+itemAmount+" "+getFormattedName(item)+"&afor &2$"+worth*itemAmount));
                }
            }
        }
    }

    @EventHandler
    public void SilkSpawnerBreak(BlockBreakEvent event){
        Player player = event.getPlayer();
        if (player.getGameMode() == GameMode.SURVIVAL){
            if (event.getBlock().getType() == Material.SPAWNER){
                event.setCancelled(true);
                if (!hasIslandAccess(player)){
                    if (!player.hasPermission("admin")){
                        event.setCancelled(true);
                        player.sendMessage(sendText("&4You don't have access to this island!"));
                    }
                    return;
                }
                player.sendMessage(sendText("&aShift+Right-Click &2to take the Spawner!"));
            }
        }
    }

    @EventHandler
    public void OnInventoryOpen(InventoryOpenEvent event){
        Player player = (Player) event.getPlayer();

        if (player.hasMetadata("gui")){
            //player.sendMessage(sendText("Opening Gui"));
        }

    }


    @EventHandler
    public void SilkSpawner(PlayerInteractEvent event){
        Player player = event.getPlayer();

        ItemStack item = player.getInventory().getItemInMainHand();


        if (item.getType() != Material.AIR){
            if (event.getAction() == Action.RIGHT_CLICK_BLOCK && player.isSneaking()){
                ItemMeta meta = item.getItemMeta();
                Block block = event.getClickedBlock();
                if (block.getType() == Material.SPAWNER){
                    if (!hasIslandAccess(player)){
                        if (!player.hasPermission("admin")){
                            player.sendMessage(sendText("&4You don't have access to remove this spawner"));
                            return;
                        }else{
                            consoleLog(sendText("&e"+player.getName()+" &6doesn't has access to the island's spawner but this player is an admin!"));
                        }
                    }

                    if (meta.hasEnchant(Enchantment.SILK_TOUCH)){
                        /*if (!player.hasPermission("skycore.silkspawner")){
                            player.sendMessage(sendText("&4You don't have permission to use silk spawner!"));
                            return;
                        }*/
                        CreatureSpawner blockSpawner = (CreatureSpawner) block.getState();

                        ItemStack getItem = new ItemStack(Material.SPAWNER);
                        BlockStateMeta getItemBlockMeta = (BlockStateMeta) getItem.getItemMeta();
                        CreatureSpawner getItemSpawner = (CreatureSpawner) getItemBlockMeta.getBlockState();
                        getItemSpawner.setSpawnedType(blockSpawner.getSpawnedType());
                        getItemBlockMeta.setBlockState(getItemSpawner);
                        getItem.setItemMeta(getItemBlockMeta);

                        ItemMeta getItemNewMeta = getItem.getItemMeta();
                        getItemNewMeta.setDisplayName(sendText("&aMob Spawner"));
                        getItem.setItemMeta(getItemNewMeta);

                        Map<Integer, ItemStack> addedItem = player.getInventory().addItem(getItem);

                        if (addedItem.isEmpty()){
                            event.getClickedBlock().setType(Material.AIR);
                            PlaySoundAt(Sound.ENTITY_EXPERIENCE_ORB_PICKUP, event.getClickedBlock().getLocation(), 1, 3);
                        }else{
                            event.setCancelled(true);
                            player.sendMessage(sendText("&4Your inventory is full!"));
                        }

                    }else{
                        event.setCancelled(true);
                        player.sendMessage(sendText("&4You must Use silktouch to break the spawner!"));
                    }
                }
            }
        }
    }

    public void OpenCatalog(Player player, int page) {
        SkyCore plugin = SkyCore.getPlugin(SkyCore.class);
        if (player.getVehicle() != null) {
            return;
        }

        player.setMetadata("gui", new FixedMetadataValue(plugin, true));
        inventoryTitle.put(player, "catalog");

        int size = 6;
        int itemsPerPage = size * 9; // Assuming 6 rows, 9 slots per row
        int start = (page - 1) * itemsPerPage;
        int end = start + itemsPerPage;

        Inventory inv = OpenGUI(player, size, "Catalog");
        player.openInventory(inv);

        ItemStack border = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta borderMeta = border.getItemMeta();
        borderMeta.setDisplayName(" ");
        border.setItemMeta(borderMeta);

        Material[] materialList = Material.values();
        List<ItemStack> availableItems = new ArrayList<>();

        for (Material material : materialList) {
            Double worth = plugin.database.GetWorth(material.toString().toLowerCase().replaceAll("_", "").trim());
            if (worth != null) {
                ItemStack catalogItem = new ItemStack(material);
                ItemMeta meta = catalogItem.getItemMeta();
                List<String> itemLore = new ArrayList<>();
                itemLore.add(sendText(""));
                itemLore.add(sendText("&7Worth: &f$" + worth));
                itemLore.add(sendText(""));
                meta.setLore(itemLore);
                catalogItem.setItemMeta(meta);
                availableItems.add(catalogItem);
            }
        }

        for (int i = 0; i < inv.getSize(); i++) {
            if (start < availableItems.size()) {
                inv.setItem(i, availableItems.get(start));
                start++;
            } else {
                break;
            }
        }
    }


    @EventHandler
    public void OnMobDeath(EntityDeathEvent event){
        Player player = event.getEntity().getKiller();
        int dropAmount = 1;
        if (player != null){
            ItemStack item = player.getInventory().getItemInMainHand();
            if (item.getType() != Material.AIR) {
                ItemMeta meta = item.getItemMeta();

                if (meta.hasEnchant(Enchantment.LOOTING)){
                    dropAmount = meta.getEnchantLevel(Enchantment.LOOTING);
                }
            }
        }

        Entity entity = event.getEntity();

        if (!(entity instanceof Mob)){
            return;
        }
        event.getDrops().clear();

        ManageMobDrops(entity, dropAmount);
    }

    public void ManageMobDrops(Entity entity, int dropAmount){
        Random random = new Random();
        int randomGet = random.nextInt(100)+1;
        Location location = entity.getLocation();

        if (entity instanceof Zombie) {
            int randomDrop = random.nextInt(7)+1;

            for (int i = 0; i < randomDrop; i++) {
                DropItem(location, new ItemStack(Material.ROTTEN_FLESH), 1);
            }
            DropExp(entity, random.nextInt(8)+1);
            PlaySound(Sound.ENTITY_ZOMBIE_DEATH, entity, 0.3f, 1);
        } else if (entity instanceof Skeleton) {
            int randomDrop = random.nextInt(8)+1;

            for (int i = 0; i < randomDrop; i++) {
                DropItem(location, new ItemStack(Material.BONE), 1);
            }
            if (randomGet <= 50){
                DropItem(location, new ItemStack(Material.ARROW), dropAmount);
            }
            DropExp(entity, random.nextInt(9)+1);
            PlaySound(Sound.ENTITY_SKELETON_DEATH, entity, 0.3f, 1);
        } else if (entity instanceof Creeper) {
            int randomDrop = random.nextInt(10)+1;

            for (int i = 0; i < randomDrop; i++) {
                DropItem(location, new ItemStack(Material.GUNPOWDER), 1);
            }
            DropExp(entity, random.nextInt(11)+1);
            PlaySound(Sound.ENTITY_CREEPER_DEATH, entity, 0.3f, 1);
        } else if (entity instanceof Spider) {
            int randomDrop = random.nextInt(9)+1;

            for (int i = 0; i < randomDrop; i++) {
                DropItem(location, new ItemStack(Material.STRING), 1);
            }
            if (randomGet <= 50){
                DropItem(location, new ItemStack(Material.SPIDER_EYE), dropAmount);
            }
            DropExp(entity, random.nextInt(10)+1);
            PlaySound(Sound.ENTITY_SPIDER_DEATH, entity, 0.3f, 1);
        } else if (entity instanceof Enderman) {
            int randomDrop = random.nextInt(11)+1;

            for (int i = 0; i < randomDrop; i++) {
                DropItem(location, new ItemStack(Material.ENDER_PEARL), 1);
            }

            if (randomGet <= 50){
                DropItem(location, new ItemStack(Material.ENDER_EYE), dropAmount);
            }
            DropExp(entity, random.nextInt(12)+1);
            PlaySound(Sound.ENTITY_ENDERMAN_DEATH, entity, 0.3f, 1);
        } else if (entity instanceof Blaze) {

            int randomDrop = random.nextInt(13)+2;
            for (int i = 0; i < randomDrop; i++) {
                DropItem(location, new ItemStack(Material.BLAZE_ROD), dropAmount);
            }
            DropExp(entity, random.nextInt(15)+1);
            PlaySound(Sound.ENTITY_BLAZE_DEATH, entity, 0.3f, 1);

        } else if (entity instanceof WitherSkeleton) {
            DropItem(location, new ItemStack(Material.COAL), dropAmount);
            if (randomGet <= 3){
                DropItem(location, new ItemStack(Material.WITHER_SKELETON_SKULL), dropAmount);
            }
            PlaySound(Sound.ENTITY_WITHER_SKELETON_DEATH, entity, 0.3f, 1);
        } else if (entity instanceof Slime) {
            int randomDrop = random.nextInt(12)+1;

            for (int i = 0; i < randomDrop; i++) {
                DropItem(location, new ItemStack(Material.SLIME_BALL), 1);
            }
            DropExp(entity, random.nextInt(13)+1);
            PlaySound(Sound.ENTITY_SLIME_DEATH, entity, 0.3f, 1);
        } else if (entity instanceof Ghast) {
            DropItem(location, new ItemStack(Material.GHAST_TEAR), dropAmount);

            PlaySound(Sound.ENTITY_GHAST_DEATH, entity, 0.3f, 1);
        } else if (entity instanceof Piglin || entity instanceof PiglinBrute) {

            int randomDrop = random.nextInt(20)+5;

            for (int i = 0; i < randomDrop; i++) {
                DropItem(location, new ItemStack(Material.GOLD_INGOT), dropAmount);
            }
            DropExp(entity, random.nextInt(22)+1);
            PlaySound(Sound.ENTITY_PIGLIN_DEATH, entity, 0.3f, 1);

        } else if (entity instanceof Hoglin) {
            DropItem(location, new ItemStack(Material.PORKCHOP), dropAmount);

            PlaySound(Sound.ENTITY_HOGLIN_DEATH, entity, 0.3f, 1);
        } else if (entity instanceof Phantom) {
            DropItem(location, new ItemStack(Material.PHANTOM_MEMBRANE), dropAmount);

            PlaySound(Sound.ENTITY_PHANTOM_DEATH, entity, 0.3f, 1);
        } else if (entity instanceof Shulker) {
            DropItem(location, new ItemStack(Material.SHULKER_SHELL), dropAmount);

            PlaySound(Sound.ENTITY_SHULKER_DEATH, entity, 0.3f, 1);
        }else if (entity instanceof Guardian) {

            int randomDrop = random.nextInt(14)+3;

            for (int i = 0; i < randomDrop; i++) {
                DropItem(location, new ItemStack(Material.PRISMARINE_SHARD), dropAmount);
            }

            if (randomGet <= 50){
                DropItem(location, new ItemStack(Material.PRISMARINE_CRYSTALS), dropAmount);
            }
            DropExp(entity, random.nextInt(18)+1);
            PlaySound(Sound.ENTITY_GUARDIAN_DEATH, entity, 0.3f, 1);
        }
        else if (entity instanceof IronGolem) {

            int randomDrop = random.nextInt(15)+4;

            for (int i = 0; i < randomDrop; i++) {
                DropItem(location, new ItemStack(Material.IRON_INGOT), dropAmount);
            }
            DropExp(entity, random.nextInt(20)+1);
            PlaySound(Sound.ENTITY_IRON_GOLEM_DEATH, entity, 0.3f, 1);
        }

        else if (entity instanceof Chicken) {
            int randomDrop = random.nextInt(2)+1;

            for (int i = 0; i < randomDrop; i++) {
                DropItem(location, new ItemStack(Material.CHICKEN), 1);
            }
            if (randomGet <= 50){
                DropItem(location, new ItemStack(Material.FEATHER), 1);
            }
            DropExp(entity, random.nextInt(3)+1);
            PlaySound(Sound.ENTITY_CHICKEN_DEATH, entity, 0.3f, 1);
        }
        else if (entity instanceof Pig) {

            int randomDrop = random.nextInt(3)+1;

            for (int i = 0; i < randomDrop; i++) {
                DropItem(location, new ItemStack(Material.PORKCHOP), 1);
            }
            DropExp(entity, random.nextInt(4)+1);
            PlaySound(Sound.ENTITY_PIG_DEATH, entity, 0.3f, 1);
        }
        else if (entity instanceof Sheep) {
            int randomDrop = random.nextInt(4)+1;

            for (int i = 0; i < randomDrop; i++) {
                DropItem(location, new ItemStack(Material.CHICKEN), 1);
            }

            if (randomGet <= 50){
                DropItem(location, new ItemStack(Material.WHITE_WOOL), 1);
            }
            DropExp(entity, random.nextInt(5)+1);
            PlaySound(Sound.ENTITY_SHEEP_DEATH, entity, 0.3f, 1);
        }
        else if (entity instanceof Cow) {
            int randomDrop = random.nextInt(5)+1;

            for (int i = 0; i < randomDrop; i++) {
                DropItem(location, new ItemStack(Material.BEEF), 1);
            }
            if (randomGet <= 50){
                DropItem(location, new ItemStack(Material.LEATHER), dropAmount);
            }
            DropExp(entity, random.nextInt(6)+1);
            PlaySound(Sound.ENTITY_COW_DEATH, entity, 0.3f, 1);
        }
        else if (entity instanceof Rabbit) {
            int randomDrop = random.nextInt(6)+1;

            for (int i = 0; i < randomDrop; i++) {
                DropItem(location, new ItemStack(Material.RABBIT), 1);
            }
            if (randomGet <= 50){
                DropItem(location, new ItemStack(Material.RABBIT_HIDE), dropAmount);
            }
            else {
                DropItem(location, new ItemStack(Material.RABBIT_FOOT), dropAmount);
            }
            DropExp(entity, random.nextInt(7)+1);
            PlaySound(Sound.ENTITY_RABBIT_DEATH, entity, 0.3f, 1);
        }

        else if (entity instanceof Warden) {
            DropItem(location, new ItemStack(Material.SCULK_CATALYST), 1);
            DropExp(entity, random.nextInt(25)+1);
            PlaySound(Sound.ENTITY_WARDEN_DEATH, entity, 0.3f, 1);
        }
    }

    public void DropItem(Location loc, ItemStack item, int amount){
        for (int i = 0; i < amount; i++) {
            loc.getWorld().dropItem(loc, item);
        }
    }

    public void DropExp(Entity entity, int amount){
        ExperienceOrb orb = entity.getWorld().spawn(entity.getLocation(), ExperienceOrb.class);
        orb.setExperience(amount);
        PlaySound(Sound.ENTITY_EXPERIENCE_ORB_PICKUP, entity, 0.3f, 1);
    }

    public void OpenMerchantMenu(Player player){
        SkyCore plugin = SkyCore.getPlugin(SkyCore.class);
        if (player.getVehicle() != null) {
            return;
        }

        inventoryTitle.put(player, "merchant");

        Inventory inv = OpenGUI(player, 2, "Merchant");

        player.openInventory(inv);

        player.getOpenInventory().setItem(0, getMerchantMenuIcon("Armorer")); //
        player.getOpenInventory().setItem(1, getMerchantMenuIcon("Librarian")); //
        player.getOpenInventory().setItem(2, getMerchantMenuIcon("Farmer"));
        player.getOpenInventory().setItem(3, getMerchantMenuIcon("Blacksmith"));
        player.getOpenInventory().setItem(4, getMerchantMenuIcon("Fisherman"));
        player.getOpenInventory().setItem(5, getMerchantMenuIcon("Cleric"));
        player.getOpenInventory().setItem(6, getMerchantMenuIcon("Butcher"));
        player.getOpenInventory().setItem(7, getMerchantMenuIcon("Cartographer"));
        player.getOpenInventory().setItem(8, getMerchantMenuIcon("Toolsmith")); //
        player.getOpenInventory().setItem(9, getMerchantMenuIcon("Leatherworker"));
        player.getOpenInventory().setItem(10, getMerchantMenuIcon("Mason"));
    }

    public ItemStack getMerchantMenuIcon(String name){
        SkyCore plugin = SkyCore.getPlugin(SkyCore.class);
        ItemStack item = new ItemStack(Material.PLAYER_HEAD);

        if (plugin.database.GetItem(name.toLowerCase()+"_icon") != null){
            item = plugin.database.GetItem(name.toLowerCase()+"_icon");
        }

        ItemMeta meta = item.getItemMeta();

        NamespacedKey key = new NamespacedKey(plugin, "merchant-category");
        meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, name);

        meta.setDisplayName(sendText("&bCategory: &a"+name));

        item.setItemMeta(meta);

        return item;
    }

    private final Set<UUID> customSpawnedEntities = new HashSet<>();

    @EventHandler
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        SkyCore plugin = SkyCore.getPlugin(SkyCore.class);
        Entity entity = event.getEntity();

        if (isDisabledMob(entity)) {
            if (event.getSpawnReason() != CreatureSpawnEvent.SpawnReason.SPAWNER
                    && event.getSpawnReason() != CreatureSpawnEvent.SpawnReason.COMMAND
                    && event.getSpawnReason() != CreatureSpawnEvent.SpawnReason.CUSTOM) {
                event.setCancelled(true);
            } else {
                if (event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.SPAWNER) {
                    event.setCancelled(true);
                    ManageMobDrops(entity, 1);
                }
            }
        }

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (!entity.hasMetadata("CustomSpawn")){
                if (entity.getCustomName() == null){
                    if (event.getSpawnReason() != CreatureSpawnEvent.SpawnReason.CUSTOM){
                        entity.remove();
                    }
                }
            }
        }, 3L);
    }

    public boolean isDisabledMob(Entity entity){
        if (entity.getType() == EntityType.IRON_GOLEM || entity.getType() == EntityType.WARDEN || entity.getType() == EntityType.PHANTOM
                || entity.getType() == EntityType.GHAST || entity.getType() == EntityType.BLAZE ||
                entity.getType() == EntityType.PIGLIN || entity.getType() == EntityType.PIGLIN_BRUTE ||
                entity.getType() == EntityType.ZOMBIFIED_PIGLIN || entity.getType() == EntityType.COW
                || entity.getType() == EntityType.SHEEP|| entity.getType() == EntityType.CHICKEN
                || entity.getType() == EntityType.PIG|| entity.getType() == EntityType.RABBIT|| entity.getType() == EntityType.GOAT
                || entity.getType() == EntityType.ZOMBIE|| entity.getType() == EntityType.SKELETON|| entity.getType() == EntityType.SPIDER
                || entity.getType() == EntityType.SLIME|| entity.getType() == EntityType.CREEPER|| entity.getType() == EntityType.GUARDIAN
                || entity.getType() == EntityType.VILLAGER || entity.getType() == EntityType.ZOMBIE_VILLAGER) {
            return true;
        }
        return false;
    }

    public double fishHeight = 7;

    /*public void SpawnFish(Player player) {
        SkyCore plugin = SkyCore.getPlugin(SkyCore.class);


        ArmorStand entity = (ArmorStand) player.getWorld().spawnEntity(player.getLocation(), EntityType.ARMOR_STAND);


        entity.getEquipment().setHelmet(plugin.database.GetItem("anglerfish"));
        entity.setCustomNameVisible(true);
        entity.setCustomName(sendText("&bAngler Fish"));
        entity.setInvisible(true);
        entity.setAI(false);
        entity.setCollidable(false);
        entity.setMarker(true);
        entity.setInvulnerable(true);
        entity.setSmall(true);

        AttributeInstance scale = entity.getAttribute(Attribute.GENERIC_SCALE);
        if (scale != null) {
            scale.setBaseValue(10);
        }

        new BukkitRunnable() {
            @Override
            public void run() {
                if (!entity.isValid() || !player.isOnline()) {
                    entity.remove();
                    this.cancel();
                    return;
                }

                if (!entity.getWorld().equals(player.getWorld())) {
                    entity.remove();
                    this.cancel();
                    return;
                }

                Location eyeLocation = player.getEyeLocation();
                Vector direction = eyeLocation.getDirection().normalize();
                Location firstLocation = eyeLocation.add(direction.multiply(6));
                Location actualLocation = firstLocation.clone().add(0, -fishHeight, 0);
                entity.teleport(actualLocation);

            }
        }.runTaskTimer(plugin, 0L, 5L);

        //DisguiseAPI.disguiseToAll(entity, disguise);

        player.sendMessage(sendText("&aSpawned fish"));
    }*/

    @EventHandler
    public void SpawnEgg(PlayerInteractEvent event){
        Player player = event.getPlayer();

        ItemStack item = player.getInventory().getItemInMainHand();
        if (item.getType() != Material.AIR){
            ItemMeta meta = item.getItemMeta();

            if (meta.hasDisplayName()){

                String displayname = uncolouredText(meta.getDisplayName());

                if (displayname.contains("Spawn Egg")) {
                    SkyCore plugin = SkyCore.getPlugin(SkyCore.class);
                    event.setCancelled(true);

                    boolean isValidEgg = false;

                    if (!meta.hasLore()){
                        return;
                    }
                    List<String> validationLore = meta.getLore();

                    for(String lore : validationLore){
                        if (lore != null){
                            if (uncolouredText(lore).contains("Type")){
                                isValidEgg = true;
                                break;
                            }
                        }
                    }

                    if (!isValidEgg){
                        return;
                    }

                    if (!hasIslandAccess(player)){
                        return;
                    }

                    if (event.getAction() == Action.RIGHT_CLICK_BLOCK || event.getAction() == Action.RIGHT_CLICK_AIR){
                        EntityType mobType = EntityType.CHICKEN;
                        String mobDisplayname = "Chicken";
                        List<String> itemLore = meta.getLore();
                        for (String lore: itemLore){
                            if (lore != null){
                                if (uncolouredText(lore).contains("Chicken")){
                                    mobType = EntityType.CHICKEN;
                                    mobDisplayname = "Chicken";
                                    break;
                                }
                                else if (uncolouredText(lore).contains("Pig")){
                                    mobType = EntityType.PIG;
                                    mobDisplayname = "Pig";
                                    break;
                                }
                                else if (uncolouredText(lore).contains("Cow")){
                                    mobType = EntityType.COW;
                                    mobDisplayname = "Cow";
                                    break;
                                }
                                else if (uncolouredText(lore).contains("Sheep")){
                                    mobType = EntityType.SHEEP;
                                    mobDisplayname = "Sheep";
                                    break;
                                }
                                else if (uncolouredText(lore).contains("Rabbit")){
                                    mobType = EntityType.RABBIT;
                                    mobDisplayname = "Rabbit";
                                    break;
                                }
                                else if (uncolouredText(lore).contains("Villager")){
                                    mobType = EntityType.VILLAGER;
                                    mobDisplayname = "Villager";
                                    break;
                                }
                                else if (uncolouredText(lore).contains("Wolf")){
                                    mobType = EntityType.WOLF;
                                    mobDisplayname = "Wolf";
                                    break;
                                }
                                else if (uncolouredText(lore).contains("Cat")){
                                    mobType = EntityType.CAT;
                                    mobDisplayname = "Cat";
                                    break;
                                }
                                else if (uncolouredText(lore).contains("Allay")){
                                    mobType = EntityType.ALLAY;
                                    mobDisplayname = "Allay";
                                    break;
                                }
                                else if (uncolouredText(lore).contains("Horse")){
                                    mobType = EntityType.HORSE;
                                    mobDisplayname = "Horse";
                                    break;
                                }
                                else if (uncolouredText(lore).contains("Camel")){
                                    mobType = EntityType.CAMEL;
                                    mobDisplayname = "Camel";
                                    break;
                                }
                                else if (uncolouredText(lore).contains("Villager")){
                                    mobType = EntityType.VILLAGER;
                                    mobDisplayname = "Villager";
                                    break;
                                }
                            }
                        }

                        SpawnMob(player, mobType, mobDisplayname);
                    }
                    item.setAmount(item.getAmount()-1);
                }
            }
        }
    }

    void SpawnMob(Player player, EntityType type, String displayname){
        SkyCore plugin = SkyCore.getPlugin(SkyCore.class);
        Entity entity = player.getWorld().spawnEntity(player.getLocation(), type);
        entity.setCustomName(sendText("&f"+displayname));
        entity.setCustomNameVisible(true);

        entity.setMetadata("CustomSpawn", new FixedMetadataValue(plugin, true));
        PlaySound(Sound.ENTITY_EXPERIENCE_ORB_PICKUP, player, 1, 1);
    }

    public void GetSpawnEgg(Player player, String type){
        ItemStack item = new ItemStack(Material.EGG);
        ItemMeta meta = item.getItemMeta();

        meta.setDisplayName(sendText("&aSpawn Egg"));
        List<String> itemLore = new ArrayList<>();
        itemLore.add(sendText("&7Type: &f"+type));

        meta.setLore(itemLore);
        meta.addEnchant(Enchantment.UNBREAKING, 1, true);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        item.setItemMeta(meta);
        player.getInventory().addItem(item);
    }

    public int getMaxEntity(Player player, EntityType type) {
        Island island = SuperiorSkyblockAPI.getPlayer(player).getIsland();
        return island.getEntityLimit(type);
    }

    public int getEntityCount(Player player, EntityType type) {
        Island island = SuperiorSkyblockAPI.getPlayer(player).getIsland();
        return island.getEntitiesTracker().getEntityCount(Key.of(type));
    }




    public HashMap<String, Merchant> merchantList = new HashMap<>();


    public void InitMerchant() {
        Random random = new Random();
        int randomEnchantLevel = random.nextInt(5)+1;
        List<ItemStack> librarianItems = Arrays.asList(
                new ItemStack(Material.PAPER),
                new ItemStack(Material.BOOKSHELF),
                new ItemStack(Material.EXPERIENCE_BOTTLE)
                , getEnchantmentBook(Enchantment.MENDING, 1), getEnchantmentBook(Enchantment.EFFICIENCY, randomEnchantLevel)
        );
        GenerateMerchant("Librarian", librarianItems);

        List<ItemStack> farmerItems = Arrays.asList(
                new ItemStack(Material.BREAD), new ItemStack(Material.BAKED_POTATO), new ItemStack(Material.POISONOUS_POTATO),
                new ItemStack(Material.PUMPKIN_PIE)
        );
        GenerateMerchant("Farmer", farmerItems);

        List<ItemStack> blacksmithItems = Arrays.asList(
                new ItemStack(Material.MAGMA_BLOCK), new ItemStack(Material.SPONGE), new ItemStack(Material.ANVIL),
                        new ItemStack(Material.NETHERITE_UPGRADE_SMITHING_TEMPLATE)
        );
        GenerateMerchant("Blacksmith", blacksmithItems);

        List<ItemStack> fishermanItems = Arrays.asList(
                new ItemStack(Material.COD), new ItemStack(Material.SALMON),
                new ItemStack(Material.TROPICAL_FISH), new ItemStack(Material.PUFFERFISH)
        );
        GenerateMerchant("Fisherman", fishermanItems);

        List<ItemStack> clericItems = Arrays.asList(
                new ItemStack(Material.GOLDEN_APPLE), new ItemStack(Material.GHAST_TEAR),
                        new ItemStack(Material.NETHER_WART), new ItemStack(Material.BLAZE_ROD),
                                new ItemStack(Material.MAGMA_CREAM)
        );
        GenerateMerchant("Cleric", clericItems);

        List<ItemStack> armorerItems = Arrays.asList(
                new ItemStack(Material.IRON_HELMET), new ItemStack(Material.IRON_CHESTPLATE),
                        new ItemStack(Material.IRON_LEGGINGS), new ItemStack(Material.IRON_BOOTS)
        );
        GenerateMerchant("Armorer", armorerItems);

        List<ItemStack> butcherItems = Arrays.asList(
                new ItemStack(Material.PORKCHOP), new ItemStack(Material.BEEF),
                        new ItemStack(Material.CHICKEN), new ItemStack(Material.MUTTON), new ItemStack(Material.RABBIT)
        );
        GenerateMerchant("Butcher", butcherItems);

        List<ItemStack> cartographerItems = Arrays.asList(
                new ItemStack(Material.COMPASS), new ItemStack(Material.CLOCK)
        );
        GenerateMerchant("Cartographer", cartographerItems);

        List<ItemStack> toolsmithItems = Arrays.asList(
                new ItemStack(Material.IRON_PICKAXE), new ItemStack(Material.IRON_AXE), new ItemStack(Material.IRON_SHOVEL),
                        new ItemStack(Material.IRON_HOE), new ItemStack(Material.IRON_SWORD)
                , new ItemStack(Material.GOLDEN_PICKAXE), new ItemStack(Material.GOLDEN_SWORD), new ItemStack(Material.GOLDEN_SHOVEL),
                        new ItemStack(Material.GOLDEN_HOE)
        );
        GenerateMerchant("Toolsmith", toolsmithItems);

        List<ItemStack> leatherworkerItems = Arrays.asList(
                new ItemStack(Material.LEATHER_HELMET), new ItemStack(Material.LEATHER_CHESTPLATE),
                        new ItemStack(Material.LEATHER_LEGGINGS), new ItemStack(Material.LEATHER_BOOTS)
        );
        GenerateMerchant("Leatherworker", leatherworkerItems);

        List<ItemStack> masonItems = Arrays.asList(
                new ItemStack(Material.GRINDSTONE), new ItemStack(Material.CRAFTING_TABLE),
                        new ItemStack(Material.FURNACE), new ItemStack(Material.CARTOGRAPHY_TABLE)
        );
        GenerateMerchant("Mason", masonItems);
    }

    public boolean isBuyableTrader(String name){
        return name.equals("Armorer")
                || name.equals("Librarian")
                || name.equals("Blacksmith")
                || name.equals("Cleric")
                || name.equals("Toolsmith")
                || name.equals("Leatherworker")
                || name.equals("Cartographer");
    }

    public boolean isSellableTrader(String name){
        return name.equals("Mason")
                || name.equals("Butcher")
                || name.equals("Farmer")
                || name.equals("Fisherman");
    }

    public void GenerateMerchant(String name, List<ItemStack> materialList){
        Random random = new Random();

        List<MerchantRecipe> recipeList = new ArrayList<>();

        int sellAmount = 1;
        int sellPrice = 1;

        ItemStack randomSell = materialList.get(random.nextInt(materialList.size()));

        // Set Buy Item

        if (isBuyableTrader(name)){
            for (ItemStack material : materialList){
                int randomSellAmount = random.nextInt(16)+1;
                int randomSellPrice = random.nextInt(3)+1;
                MerchantRecipe trade = null;
                int priceAdd = 0;

                if (material.getType().getMaxStackSize() > 1){
                    material.setAmount(randomSellAmount);
                    if (material.getType().equals(Material.NETHERITE_UPGRADE_SMITHING_TEMPLATE)){
                        material.setAmount(1);
                    }
                }else{
                    material.setAmount(1);
                }

                if (material.equals(new ItemStack(Material.NETHERITE_UPGRADE_SMITHING_TEMPLATE))){
                    priceAdd = 64;
                }

                else if (material.getType() == Material.ENCHANTED_BOOK){
                    priceAdd = 64;
                }
                trade = new MerchantRecipe(material, 10000);

                int totalItemPrice = randomSellPrice+priceAdd;

                if (totalItemPrice > 64){
                    totalItemPrice = 64;
                }

                trade.addIngredient(new ItemStack(Material.EMERALD, totalItemPrice));
                recipeList.add(trade);

                if (material.getType() == randomSell.getType()) {
                    if (material.getType().getMaxStackSize() > 1){
                        sellAmount = randomSellAmount;
                        sellPrice = randomSellPrice;
                    }else{
                        sellAmount = 1;
                        sellPrice = randomSellPrice;
                    }
                }
            }
        }
        int multiplySellItem = 1;

        if (name.equals("Mason")){
            multiplySellItem = 5;
        }

        else if (name.equals("Butcher")
                || name.equals("Fisherman")|| name.equals("Cleric")
                || name.equals("Farmer")){
            multiplySellItem = 3;
        }

        // Set Sell Item
        MerchantRecipe trade;
        if (isSellableTrader(name)){
            trade = new MerchantRecipe(new ItemStack(Material.EMERALD, sellPrice), 10000);
            int totalSellAmount = sellAmount*multiplySellItem;
            if (totalSellAmount > 64){
                totalSellAmount = 64;
            }
            if (randomSell.getType().getMaxStackSize() > 1) {
                randomSell.setAmount(totalSellAmount);
            }
            trade.addIngredient(randomSell);

            recipeList.add(trade);
        }

        merchantList.putIfAbsent(name, Bukkit.createMerchant(name));
        merchantList.get(name).setRecipes(recipeList);
    }

    public void OpenMerchant(Player player, String type){
        Merchant merchant = merchantList.get(type);
        player.openMerchant(merchant, false);
    }

    public ItemStack getEnchantmentBook(Enchantment customEnchantment, int level) {
        // Create an enchanted book
        ItemStack enchantedBook = new ItemStack(Material.ENCHANTED_BOOK);

        // Get the meta for the enchanted book
        EnchantmentStorageMeta meta = (EnchantmentStorageMeta) enchantedBook.getItemMeta();

        if (meta != null) {
            // Add your custom enchantment
            meta.addStoredEnchant(customEnchantment, level, true);

            // Set the meta back to the item
            enchantedBook.setItemMeta(meta);
        }

        return enchantedBook;
    }

    @EventHandler
    public void onIslandKick(IslandKickEvent event) {
        // Get the player who initiated the kick
        String kicker = event.getPlayer().getName();

        // Get the player who was kicked
        Player kickedPlayer = (Player) event.getPlayer();


    }

    public boolean hasIslandAccess(Player player) {
        // Get the island at the given location
        Island island = SuperiorSkyblockAPI.getGrid().getIslandAt(player.getLocation());

        // Get the SuperiorPlayer instance for the given player
        SuperiorPlayer superiorPlayer = SuperiorSkyblockAPI.getPlayer(player);

        // If the island is null, the block isn't part of any island
        if (island == null) {
            return false;
        }

        // Check if the player is a member or coop member of the island
        return island.isMember(superiorPlayer) || island.isCoop(superiorPlayer);
    }

}
