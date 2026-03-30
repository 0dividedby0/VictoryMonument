# Victory Monument Mod

A Minecraft Forge mod for Minecraft 1.20.1 that spawns a victory monument at world spawn with 16 colored dungeons containing wool samples. Collect all wool colors to achieve victory!

## Features

- Spawns a central victory monument at world spawn
- Generates 16 random dungeons (one per wool color) within a radius of spawn
- Dungeons are designed to be challenging with infernal-style mobs
- Collect all 16 wool colors by placing them on the monument to win

## Prerequisites

- macOS (based on development environment)
- Homebrew (package manager for macOS)
- OpenJDK 17
- Gradle (wrapper included)
- Minecraft Forge MDK 1.20.1-47.4.10

## Setup Instructions

### 1. Install Homebrew

If you don't have Homebrew installed:

```bash
/bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)"
```

Follow the on-screen instructions to complete the installation.

More info: [Homebrew Official Site](https://brew.sh/)

### 2. Install OpenJDK 17

```bash
brew install openjdk@17
echo 'export PATH="/usr/local/opt/openjdk@17/bin:$PATH"' >> ~/.zshrc
source ~/.zshrc
```

If using bash instead of zsh:

```bash
echo 'export PATH="/usr/local/opt/openjdk@17/bin:$PATH"' >> ~/.bashrc
source ~/.bashrc
```

Accept Xcode license if prompted:

```bash
sudo xcodebuild -license accept
```

Create symlink for Java Virtual Machine:

```bash
sudo ln -sfn /usr/local/opt/openjdk@17/libexec/openjdk.jdk /Library/Java/JavaVirtualMachines/openjdk-17.jdk
```

### 3. Install Gradle (Optional - Wrapper Included)

The project includes a Gradle wrapper, so you don't need to install Gradle globally. However, if you prefer:

```bash
brew install gradle
```

### 4. Set Up Forge MDK

This project is based on the Forge MDK for 1.20.1-47.4.10. If starting from scratch:

1. Download the MDK from: [Forge Files](https://files.minecraftforge.net/net/minecraftforge/forge/index_1.20.1.html)
2. Extract the MDK zip file
3. Copy the contents to your project directory, replacing existing files as needed
4. Run initial setup:

```bash
./gradlew genEclipseRuns  # or genIntellijRuns for IntelliJ
./gradlew genSources
```

### 5. Build the Project

```bash
./gradlew build
```

This will compile the mod and generate the JAR file at `build/libs/victorymod-1.0.0.jar`. Copy this JAR to your Prism Launcher instance's mods folder to install the mod.

## Running the Mod

### Using VS Code Tasks

If using VS Code, use the configured tasks:

- **Run Client**: Launches Minecraft with the mod
- **Build**: Compiles the mod
- **Gen Eclipse Runs**: Generates Eclipse run configurations

### Manual Commands

- Run the client: `./gradlew runClient`
- Build: `./gradlew build`
- Generate Eclipse runs: `./gradlew genEclipseRuns`
- Generate IntelliJ runs: `./gradlew genIntellijRuns`
- Generate VS Code runs: `./gradlew :genVSCodeRuns`

## Development

### Project Structure

- `src/main/java/com/dividedby0/victorymod/`: Main mod code
  - `VictoryMod.java`: Main mod class
  - `WorldInit.java`: Handles world generation events
  - `StructureSpawner.java`: Spawns structures
  - `MonumentTracker.java`: Tracks wool collection
  - `PlayerData.java`: Manages player progress
- `src/main/resources/`: Resources and data files
  - `data/victorymod/structures/`: NBT structure files
  - `META-INF/mods.toml`: Mod metadata

### Key Classes

- **VictoryMod**: Entry point, registers event handlers
- **WorldInit**: Listens for world load events and triggers structure spawning
- **StructureSpawner**: Places victory monument and dungeons at spawn
- **MonumentTracker**: Detects wool placement and checks victory conditions
- **PlayerData**: Tracks collected wool per player

### Adding New Features

1. Modify the relevant class files
2. Update resources in `src/main/resources/`
3. Run `./gradlew build` to compile
4. Test with `./gradlew runClient`

## Troubleshooting

- If you get Java version errors, ensure OpenJDK 17 is properly installed and PATH is set
- For Gradle issues, try `./gradlew --no-daemon clean build`
- If structures don't spawn, check console logs for error messages
- Ensure you're using the correct Forge version (47.4.10 for 1.20.1)

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Test thoroughly
5. Submit a pull request

## License

All Rights Reserved (as per mod metadata)