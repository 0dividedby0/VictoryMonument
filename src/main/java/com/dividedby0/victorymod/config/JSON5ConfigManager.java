package com.dividedby0.victorymod.config;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Manages JSON5 configuration files for Victory Mod.
 * Uses a simple map-based approach for configuration storage.
 */
public class JSON5ConfigManager {
    private static final String CONFIG_FILENAME = "victorymod.json5";
    private final Path configPath;
    private final Map<String, Object> configData;
    
    // Configuration metadata for UI rendering
    public static class ConfigEntry {
        public String key;
        public Object value;
        public Object minValue;
        public Object maxValue;
        public String description;
        public String type; // "int", "string", "boolean"
        
        public ConfigEntry(String key, Object value, String type, String description) {
            this.key = key;
            this.value = value;
            this.type = type;
            this.description = description;
        }
    }
    
    private final Map<String, ConfigEntry> configMetadata;
    
    public JSON5ConfigManager(Path configDir) {
        this.configPath = configDir.resolve(CONFIG_FILENAME);
        this.configData = new HashMap<>();
        this.configMetadata = new HashMap<>();
        initializeMetadata();
        loadConfig();
    }
    
    /**
     * Initializes metadata for configuration entries (used by UI)
     */
    private void initializeMetadata() {
        configMetadata.put("minDungeonRadius", new ConfigEntry(
            "minDungeonRadius", 40, "int", 
            "Minimum radius for dungeon placement around spawn point (in blocks)"
        ));
        configMetadata.get("minDungeonRadius").minValue = 10;
        configMetadata.get("minDungeonRadius").maxValue = 500;
        
        configMetadata.put("maxDungeonRadius", new ConfigEntry(
            "maxDungeonRadius", 750, "int",
            "Maximum radius for dungeon placement around spawn point (in blocks)"
        ));
        configMetadata.get("maxDungeonRadius").minValue = 50;
        configMetadata.get("maxDungeonRadius").maxValue = 1000;
        
        configMetadata.put("structureBufferDistance", new ConfigEntry(
            "structureBufferDistance", 30, "int",
            "Minimum buffer distance between structures to prevent overlap (in blocks)"
        ));
        configMetadata.get("structureBufferDistance").minValue = 5;
        configMetadata.get("structureBufferDistance").maxValue = 200;
    }
    
    /**
     * Loads configuration from JSON5 file, or creates defaults if file doesn't exist
     */
    private void loadConfig() {
        createDefaults();
        
        if (Files.exists(configPath)) {
            try {
                String content = Files.readString(configPath);
                parseJSON5(content);
            } catch (IOException e) {
                System.err.println("Error reading config file: " + e.getMessage());
            }
        } else {
            saveConfig();
        }
    }
    
    /**
     * Simple JSON5 parser (handles basic JSON with comments)
     */
    private void parseJSON5(String json) {
        // Remove comments
        json = json.replaceAll("//.*?\\n", "\n");
        json = json.replaceAll("/\\*.*?\\*/", "");
        
        // Extract key-value pairs
        Pattern pattern = Pattern.compile("\"([^\"]+)\"\\s*:\\s*([^,}]+)");
        var matcher = pattern.matcher(json);
        
        while (matcher.find()) {
            String key = matcher.group(1);
            String valueStr = matcher.group(2).trim();
            
            // Parse value
            Object value = parseValue(valueStr);
            if (value != null && configMetadata.containsKey(key)) {
                configData.put(key, value);
                configMetadata.get(key).value = value;
            }
        }
    }
    
    /**
     * Parse individual values from JSON
     */
    private Object parseValue(String valueStr) {
        valueStr = valueStr.replaceAll("[,\\s]+$", ""); // Remove trailing comma and whitespace
        
        if (valueStr.equalsIgnoreCase("true")) return true;
        if (valueStr.equalsIgnoreCase("false")) return false;
        if (valueStr.startsWith("\"") && valueStr.endsWith("\"")) {
            return valueStr.substring(1, valueStr.length() - 1);
        }
        
        try {
            if (valueStr.contains(".")) {
                return Double.parseDouble(valueStr);
            } else {
                return Integer.parseInt(valueStr);
            }
        } catch (NumberFormatException e) {
            return null;
        }
    }
    
    /**
     * Creates default configuration values
     */
    private void createDefaults() {
        for (String key : configMetadata.keySet()) {
            configData.put(key, configMetadata.get(key).value);
        }
    }
    
    /**
     * Saves configuration to JSON5 file
     */
    public void saveConfig() {
        try {
            Files.createDirectories(configPath.getParent());
            StringBuilder json = new StringBuilder("{\n");
            
            boolean first = true;
            for (String key : configData.keySet()) {
                if (!first) json.append(",\n");
                first = false;
                
                ConfigEntry entry = configMetadata.get(key);
                json.append("  // ").append(entry.description).append("\n");
                json.append("  \"").append(key).append("\": ");
                
                Object value = configData.get(key);
                if (value instanceof String) {
                    json.append("\"").append(value).append("\"");
                } else {
                    json.append(value);
                }
            }
            
            json.append("\n}\n");
            Files.writeString(configPath, json.toString());
        } catch (IOException e) {
            System.err.println("Error saving config file: " + e.getMessage());
        }
    }
    
    /**
     * Gets an integer value from config
     */
    public int getInt(String key, int defaultValue) {
        try {
            Object val = configData.get(key);
            if (val instanceof Integer) return (Integer) val;
            if (val instanceof Number) return ((Number) val).intValue();
            return defaultValue;
        } catch (Exception e) {
            return defaultValue;
        }
    }
    
    /**
     * Sets an integer value in config
     */
    public void setInt(String key, int value) {
        configData.put(key, value);
        if (configMetadata.containsKey(key)) {
            configMetadata.get(key).value = value;
        }
    }
    
    /**
     * Gets a string value from config
     */
    public String getString(String key, String defaultValue) {
        try {
            Object val = configData.get(key);
            if (val instanceof String) return (String) val;
            if (val != null) return val.toString();
            return defaultValue;
        } catch (Exception e) {
            return defaultValue;
        }
    }
    
    /**
     * Sets a string value in config
     */
    public void setString(String key, String value) {
        configData.put(key, value);
        if (configMetadata.containsKey(key)) {
            configMetadata.get(key).value = value;
        }
    }
    
    /**
     * Gets a boolean value from config
     */
    public boolean getBoolean(String key, boolean defaultValue) {
        try {
            Object val = configData.get(key);
            if (val instanceof Boolean) return (Boolean) val;
            return defaultValue;
        } catch (Exception e) {
            return defaultValue;
        }
    }
    
    /**
     * Sets a boolean value in config
     */
    public void setBoolean(String key, boolean value) {
        configData.put(key, value);
        if (configMetadata.containsKey(key)) {
            configMetadata.get(key).value = value;
        }
    }
    
    /**
     * Gets all configuration keys
     */
    public String[] getAllKeys() {
        return configData.keySet().toArray(new String[0]);
    }
    
    /**
     * Gets all configuration entries with metadata
     */
    public Map<String, ConfigEntry> getConfigMetadata() {
        return configMetadata;
    }
    
    /**
     * Gets the config as a map for easy iteration
     */
    public Map<String, Object> getAllConfig() {
        return new HashMap<>(configData);
    }
}

