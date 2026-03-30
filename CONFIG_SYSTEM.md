# Victory Mod - In-Game Configuration System

## Overview

Victory Mod now features a complete in-game configuration UI that allows players to modify mod settings without restarting the game. Configuration is stored in JSON5 format for easy editing.

## Features

- **In-Game UI**: Access configuration from the pause menu during gameplay
- **JSON5 Format**: Human-readable configuration with support for comments
- **Validation**: Built-in min/max validation for numeric values
- **Persistence**: Changes are automatically saved to disk
- **Type Safety**: Automatic type conversion with fallback defaults

## Configuration File

The configuration file is located at:
```
<minecraft_directory>/config/victorymod.json5
```

### Default Configuration

```json5
{
  // Minimum radius for dungeon placement around spawn point (in blocks)
  "minDungeonRadius": 40,
  // Maximum radius for dungeon placement around spawn point (in blocks)
  "maxDungeonRadius": 750,
  // Minimum buffer distance between structures to prevent overlap (in blocks)
  "structureBufferDistance": 30
}
```

### Configuration Parameters

| Parameter | Type | Range | Default | Description |
|-----------|------|-------|---------|-------------|
| `minDungeonRadius` | Integer | 10-500 | 40 | Minimum distance from spawn point where dungeons can be placed |
| `maxDungeonRadius` | Integer | 50-1000 | 750 | Maximum distance from spawn point where dungeons can be placed |
| `structureBufferDistance` | Integer | 5-200 | 30 | Minimum distance between adjacent dungeon structures |

## How to Use

### In-Game Configuration

1. Press `ESC` during gameplay to open the pause menu
2. Click the **"Victory Config"** button
3. Modify the desired settings using the text fields
4. Click **"Save & Close"** to apply changes
5. Changes take effect immediately

### Manual Configuration

1. Navigate to `<minecraft_directory>/config/`
2. Open `victorymod.json5` with any text editor
3. Modify the values as needed
4. Save the file
5. Reload the mod or restart Minecraft

## JSON5 Format

The configuration system supports JSON5, which allows:

- **Comments**: Use `//` for single-line comments and `/* */` for multi-line comments
- **Trailing Commas**: Optional commas after the last item in objects
- **Unquoted Keys**: Keys don't need quotes (but strings do)
- **Single Quotes**: Strings can use single quotes (though double quotes work too)

### Example with Advanced Features

```json5
{
  // Set minimum spawn distance
  minDungeonRadius: 40,
  
  /* Set maximum spawn distance
     This controls how far from spawn dungeons can be placed */
  maxDungeonRadius: 750,
  
  // Structure buffer distance
  structureBufferDistance: 30,
}
```

## Architecture

### Configuration System Components

- **JSON5ConfigManager**: Handles loading, parsing, and saving JSON5 files
- **ConfigManager**: Singleton pattern for global config instance
- **VictoryConfigScreen**: In-game UI screen for editing configuration
- **ConfigFieldWidget**: Individual input field for each configuration parameter
- **ConfigClientEvents**: Event handler that adds the config button to pause menu

### File Locations

```
src/main/java/com/dividedby0/victorymod/
├── config/
│   ├── JSON5ConfigManager.java       # JSON5 file I/O
│   ├── ConfigManager.java            # Singleton instance
│   ├── ConfigClientEvents.java       # Client event handler
│   └── screen/
│       ├── VictoryConfigScreen.java  # Main config GUI
│       └── ConfigFieldWidget.java    # Input field widget
```

## Technical Details

### JSON5 Parsing

The system includes a simple JSON5 parser that:
- Strips single-line (`//`) and multi-line (`/* */`) comments
- Parses boolean, integer, and string values
- Validates values against configured min/max ranges
- Falls back to defaults for invalid or missing values

### Configuration Metadata

Each configuration entry includes:
- **Key**: Parameter name
- **Value**: Current value
- **Type**: Data type (int, string, boolean)
- **Min/Max**: Valid value range
- **Description**: Human-readable description

### Runtime Access

Access configuration values in your code:

```java
JSON5ConfigManager config = ConfigManager.getInstance();

// Get values with defaults
int minRadius = config.getInt("minDungeonRadius", 40);
String name = config.getString("dungeonName", "");
boolean enabled = config.getBoolean("feature", true);

// Set values (changes in memory)
config.setInt("minDungeonRadius", 50);

// Persist to disk
config.saveConfig();
```

## Future Enhancements

Potential improvements to the configuration system:

- [ ] GUI sliders for numeric values
- [ ] Nested configuration sections
- [ ] Configuration profiles/presets
- [ ] Advanced validators (regex, custom functions)
- [ ] Per-world configuration
- [ ] Configuration synchronization with server
- [ ] Mod options from the mod menu (already supported by Forge)

## Troubleshooting

### Config file not found

If the configuration file doesn't exist, it will be automatically created with default values on first load.

### Invalid values

If you enter invalid values in the UI:
- Invalid integers will trigger an error message
- The field will revert to the previous valid value
- Values outside min/max range are automatically clamped

### Configuration not applying

- Ensure you clicked **"Save & Close"** and not **"Cancel"**
- Check the console for error messages
- Verify the JSON5 file syntax if editing manually

### Changes lost on restart

If changes are lost when restarting:
1. Check that `victorymod.json5` exists in `config/` folder
2. Verify file has write permissions
3. Check console for save errors
4. Try using the in-game UI instead of manual editing
