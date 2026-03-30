package com.dividedby0.victorymod;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.client.ConfigScreenHandler;
import com.dividedby0.victorymod.config.ConfigManager;

@Mod(VictoryMod.MODID)
public class VictoryMod {
    public static final String MODID = "victorymod";

    public VictoryMod() {
        // Initialize JSON5 config manager
        ConfigManager.getInstance();
        
        // Register config screen factory for the mods menu config button
        ModLoadingContext.get().registerExtensionPoint(
            ConfigScreenHandler.ConfigScreenFactory.class,
            () -> new ConfigScreenHandler.ConfigScreenFactory(
                (minecraft, screen) -> new SimpleConfigScreen(screen, ConfigManager.getInstance())
            )
        );
        
        WorldInit.init();
        MonumentTracker.init();
    }
}