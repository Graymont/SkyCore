package org.skycore.skycore;

import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public final class SkyCore extends JavaPlugin {

    public Events events = new Events();
    public Database database = new Database(this.events);
    public Commands commands = new Commands(this.events, this.database);

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(events, this);
        database.LoadAllData();
        RegisterCommands();
        events.setupEconomy();
        events.InitMerchant();
    }

    @Override
    public void onDisable() {
        database.SaveAllData();
    }

    public void RegisterCommands(){
        Objects.requireNonNull(getCommand("skycore")).setExecutor(this.commands);
        Objects.requireNonNull(getCommand("sellitem")).setExecutor(this.commands);
        Objects.requireNonNull(getCommand("worth")).setExecutor(this.commands);
        Objects.requireNonNull(getCommand("shop")).setExecutor(this.commands);
        Objects.requireNonNull(getCommand("sellall")).setExecutor(this.commands);
        Objects.requireNonNull(getCommand("catalog")).setExecutor(this.commands);
        Objects.requireNonNull(getCommand("buyamount")).setExecutor(this.commands);

        Objects.requireNonNull(getCommand("skycore")).setTabCompleter(this.commands);
        Objects.requireNonNull(getCommand("buyamount")).setTabCompleter(this.commands);
    }
}
