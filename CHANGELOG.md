# Changelog

All notable changes to this project will be documented in this file.

The format is based on Keep a Changelog and this project follows semantic versioning.

## [1.0.0-beta.2] - 2026-04-18

### Added
- Per-structure spawn rules in `victorymod.json5` for biome filtering and height selection.
- Support for `surface`, `underground`, `air`, and `fixed` height modes on both the victory monument and individual dungeons.
- Biome allow/deny lists using biome ids or biome tags for structure-specific placement control.

### Changed
- Updated config initialization to use Forge's config directory path so the mod behaves correctly in shared dev-pack runs and other nonstandard launch environments.

## [1.0.0-beta.1] - 2026-04-02

### Added
- Victory Monument structure spawns near world spawn on first load.
- 16 color-coded dungeons (one per wool color) placed randomly around the monument.
- Configurable dungeon radius (min/max) and buffer distance between structures.
- Wool-carpet collection tracker: placing all 16 colors triggers the victory message.
- Server-side persistence prevents structure re-spawning across restarts.
- In-game configuration screen for dungeon placement settings.
- Hard infernal-style mob setups inside dungeons.
