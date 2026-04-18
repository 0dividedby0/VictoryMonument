package com.dividedby0.victorymod.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Manages JSON5 configuration files for Victory Monument.
 * Uses a simple map-based approach for configuration storage.
 */
public class JSON5ConfigManager {
    private static final String CONFIG_FILENAME = "victorymod.json5";
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private final Path configPath;
    private final Map<String, Object> configData;

    // Configuration metadata for UI rendering
    public static class ConfigEntry {
        public String key;
        public Object value;
        public Object minValue;
        public Object maxValue;
        public String description;
        public String type; // "int", "string", "boolean", "object"

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
        this.configData = new LinkedHashMap<>();
        this.configMetadata = new LinkedHashMap<>();
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

        configMetadata.put("defaultRules", new ConfigEntry(
            "defaultRules", createDefaultRules(), "object",
            "Default biome, height, and placement rules used by structures unless overridden"
        ));

        configMetadata.put("structures", new ConfigEntry(
            "structures", createDefaultStructures(), "object",
            "Per-structure placement rule overrides for the monument and each dungeon"
        ));
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
     * Parses JSON5-like content by stripping comments and trailing commas before using Gson.
     */
    private void parseJSON5(String json) {
        json = stripComments(json);
        json = removeTrailingCommas(json);

        JsonElement rootElement = JsonParser.parseString(json);
        if (!rootElement.isJsonObject()) {
            return;
        }

        JsonObject root = rootElement.getAsJsonObject();
        for (String key : configMetadata.keySet()) {
            if (!root.has(key)) {
                continue;
            }

            Object parsedValue = parseElement(root.get(key));
            if (parsedValue == null) {
                continue;
            }

            configData.put(key, parsedValue);
            configMetadata.get(key).value = cloneValue(parsedValue);
        }
    }

    /**
     * Creates default configuration values
     */
    private void createDefaults() {
        for (String key : configMetadata.keySet()) {
            configData.put(key, cloneValue(configMetadata.get(key).value));
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
                if (!first) {
                    json.append(",\n");
                }
                first = false;

                ConfigEntry entry = configMetadata.get(key);
                json.append("  // ").append(entry.description).append("\n");
                json.append("  \"").append(key).append("\": ");

                Object value = configData.get(key);
                if (value instanceof JsonElement jsonElement) {
                    json.append(GSON.toJson(jsonElement).replace("\n", "\n  "));
                } else if (value instanceof String) {
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
            if (val instanceof Integer) {
                return (Integer) val;
            }
            if (val instanceof Number) {
                return ((Number) val).intValue();
            }
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
            if (val instanceof String) {
                return (String) val;
            }
            if (val != null) {
                return val.toString();
            }
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
            if (val instanceof Boolean) {
                return (Boolean) val;
            }
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

    public JsonObject getJsonObject(String key, JsonObject defaultValue) {
        Object val = configData.get(key);
        if (val instanceof JsonObject jsonObject) {
            return jsonObject.deepCopy();
        }
        return defaultValue.deepCopy();
    }

    public void setJsonObject(String key, JsonObject value) {
        JsonObject copy = value.deepCopy();
        configData.put(key, copy);
        if (configMetadata.containsKey(key)) {
            configMetadata.get(key).value = copy.deepCopy();
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
        return new LinkedHashMap<>(configData);
    }

    private static String stripComments(String json) {
        json = json.replaceAll("(?s)/\\*.*?\\*/", "");
        return json.replaceAll("(?m)//.*$", "");
    }

    private static String removeTrailingCommas(String json) {
        return json.replaceAll(",\\s*([}\\]])", "$1");
    }

    private static Object parseElement(JsonElement element) {
        if (element == null || element.isJsonNull()) {
            return null;
        }
        if (element.isJsonObject()) {
            return element.getAsJsonObject().deepCopy();
        }
        if (element.isJsonArray()) {
            return element.getAsJsonArray().deepCopy();
        }
        if (element.isJsonPrimitive()) {
            var primitive = element.getAsJsonPrimitive();
            if (primitive.isBoolean()) {
                return primitive.getAsBoolean();
            }
            if (primitive.isNumber()) {
                Number number = primitive.getAsNumber();
                double value = number.doubleValue();
                if (value == Math.rint(value)) {
                    return number.intValue();
                }
                return value;
            }
            if (primitive.isString()) {
                return primitive.getAsString();
            }
        }
        return null;
    }

    private static Object cloneValue(Object value) {
        if (value instanceof JsonObject jsonObject) {
            return jsonObject.deepCopy();
        }
        if (value instanceof JsonArray jsonArray) {
            return jsonArray.deepCopy();
        }
        return value;
    }

    private static JsonObject createDefaultRules() {
        JsonObject root = new JsonObject();

        JsonObject biomes = new JsonObject();
        biomes.addProperty("mode", "any");
        biomes.add("values", new JsonArray());
        root.add("biomes", biomes);

        JsonObject height = new JsonObject();
        height.addProperty("mode", "surface");
        height.addProperty("minY", 40);
        height.addProperty("maxY", 120);
        height.addProperty("y", 64);
        height.addProperty("surfaceOffset", 0);
        root.add("height", height);

        JsonObject placement = new JsonObject();
        placement.addProperty("requireSolidGround", true);
        placement.addProperty("allowWater", false);
        placement.addProperty("allowTrees", false);
        root.add("placement", placement);

        return root;
    }

    private static JsonObject createDefaultStructures() {
        JsonObject structures = new JsonObject();
        structures.add("victory_monument", new JsonObject());
        structures.add("dungeon_white", new JsonObject());
        structures.add("dungeon_orange", new JsonObject());
        structures.add("dungeon_magenta", new JsonObject());
        structures.add("dungeon_lightblue", new JsonObject());
        structures.add("dungeon_yellow", new JsonObject());
        structures.add("dungeon_lime", new JsonObject());
        structures.add("dungeon_pink", new JsonObject());
        structures.add("dungeon_gray", new JsonObject());
        structures.add("dungeon_lightgray", new JsonObject());
        structures.add("dungeon_cyan", new JsonObject());
        structures.add("dungeon_purple", new JsonObject());
        structures.add("dungeon_blue", new JsonObject());
        structures.add("dungeon_brown", new JsonObject());
        structures.add("dungeon_green", new JsonObject());
        structures.add("dungeon_red", new JsonObject());
        structures.add("dungeon_black", new JsonObject());
        return structures;
    }
}
