package com.zyunsoftware.capydevmc.app;

import java.util.logging.Logger;

import org.bukkit.plugin.java.JavaPlugin;

import com.zyunsoftware.capydevmc.app.actions.commands.TeleportWorldCommand;

public class CapylandPlugin extends JavaPlugin {
    private static CapylandPlugin _instancePlugin;

    private Logger _logger;

    public CapylandPlugin() {
        saveDefaultConfig();
        _instancePlugin = this;
        _logger = getLogger();
    }

    @Override
    public void onEnable() {
        DependencyInjection.getMigrationRepository().migrate();

        getCommand("teleportworld").setExecutor(new TeleportWorldCommand());
    }

    @Override
    public void onDisable() {
        _logger.info("Плагін вимкнено.");
    }

    public static CapylandPlugin getInstance() {
        return _instancePlugin;
    }
}
