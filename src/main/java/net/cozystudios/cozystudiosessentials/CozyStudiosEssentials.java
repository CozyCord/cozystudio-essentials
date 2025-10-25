package net.cozystudios.cozystudiosessentials;

import net.cozystudios.cozystudiosessentials.command.HomeCommand;
import net.cozystudios.cozystudiosessentials.data.PlayerDataManager;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CozyStudiosEssentials implements ModInitializer {
    public static final String MOD_ID = "cozystudiosessentials";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private static PlayerDataManager playerDataManager;

    @Override
    public void onInitialize() {
        LOGGER.info("Initializing Cozy Studios Essentials!");

        // Register commands
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            HomeCommand.register(dispatcher);
        });

        // Initialize player data manager when server starts
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            playerDataManager = new PlayerDataManager(server);
        });

        // Save all player data on server stop
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            if (playerDataManager != null) {
                playerDataManager.saveAll();
            }
        });

        // TODO: Implement auto-save system with a scheduled task
    }

    public static PlayerDataManager getPlayerDataManager() {
        return playerDataManager;
    }
}