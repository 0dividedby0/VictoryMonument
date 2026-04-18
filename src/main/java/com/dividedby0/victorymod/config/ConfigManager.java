package com.dividedby0.victorymod.config;

import net.minecraftforge.fml.loading.FMLPaths;

/**
 * Manages the global config instance for the mod
 */
public class ConfigManager {
    private static JSON5ConfigManager instance = null;
    
    /**
     * Gets or creates the config manager singleton
     */
    public static JSON5ConfigManager getInstance() {
        if (instance == null) {
            try {
                java.nio.file.Path configPath = FMLPaths.CONFIGDIR.get();
                instance = new JSON5ConfigManager(configPath);
            } catch (Exception e) {
                System.err.println("Failed to initialize config manager: " + e.getMessage());
                e.printStackTrace();
                // Create a fallback with system temp directory
                try {
                    instance = new JSON5ConfigManager(java.nio.file.Paths.get(System.getProperty("java.io.tmpdir")));
                } catch (Exception ex) {
                    System.err.println("Failed to create fallback config manager: " + ex.getMessage());
                }
            }
        }
        return instance;
    }
    
    /**
     * Reloads the config from disk
     */
    public static void reload() {
        // Create a fresh instance
        instance = null;
        getInstance();
    }
}
