package com.dividedby0.victorymod;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import com.dividedby0.victorymod.config.ConfigManager;
import com.dividedby0.victorymod.config.JSON5ConfigManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class StructureSpawner {

    private static final String[] COLORS = {
        "white","orange","magenta","lightblue","yellow","lime","pink","gray",
        "lightgray","cyan","purple","blue","brown","green","red","black"
    };

    private static final List<BlockPos> placedStructures = new ArrayList<>();

    public static void spawnAll(ServerLevel level) {
        placedStructures.clear();
        
        BlockPos spawn = level.getSharedSpawnPos();

        int spawnX = spawn.getX();
        int spawnZ = spawn.getZ();
        int spawnY = getGroundY(level, spawnX, spawnZ);

        BlockPos victoryPos = new BlockPos(spawnX, spawnY, spawnZ);
        
        // Try to find a valid location for the victory monument
        BlockPos validMonumentPos = victoryPos;
        if (!isValidSpawnLocation(level, victoryPos)) {
            // If exact spawn is invalid, search nearby for a valid location
            validMonumentPos = findNearbyValidLocation(level, spawnX, spawnZ, 100);
            if (validMonumentPos == null) {
                System.err.println("[VictoryMod] Could not find valid spawn location for victory monument!");
                // Force place at spawn anyway as fallback
                validMonumentPos = victoryPos;
            }
        }
        
        placeStructure(level, "victory_monument", validMonumentPos);
        placedStructures.add(validMonumentPos);

        Random rand = new Random();
        // Read configuration from JSON5 config
        JSON5ConfigManager configManager = ConfigManager.getInstance();
        int minRadius = configManager.getInt("minDungeonRadius", 40);
        int maxRadius = configManager.getInt("maxDungeonRadius", 750);
        int bufferDistance = configManager.getInt("structureBufferDistance", 30);
        
        for (String color : COLORS) {
            BlockPos dungeonPos = spawnDungeonWithFallbacks(level, rand, spawnX, spawnZ, color, minRadius, maxRadius, bufferDistance);
            if (dungeonPos != null) {
                placedStructures.add(dungeonPos);
            }
        }
    }

    /**
     * Spawn a dungeon with progressive fallback strategies to ensure placement.
     * Tries in order: valid location + proximity check, valid location only, 
     * any location + proximity check, any location.
     */
    private static BlockPos spawnDungeonWithFallbacks(ServerLevel level, Random rand, int spawnX, int spawnZ, 
                                                       String color, int minRadius, int maxRadius, int bufferDistance) {
        // Strategy 1: Valid location + respect buffer distance
        BlockPos pos = findDungeonLocation(level, rand, spawnX, spawnZ, minRadius, maxRadius, bufferDistance, true, true);
        if (pos != null) {
            placeStructure(level, "dungeon_" + color, pos);
            return pos;
        }
        
        // Strategy 2: Valid location only (ignore buffer)
        System.out.println("[VictoryMod] Dungeon_" + color + " relaxing buffer constraint");
        pos = findDungeonLocation(level, rand, spawnX, spawnZ, minRadius, maxRadius, 0, true, true);
        if (pos != null) {
            placeStructure(level, "dungeon_" + color, pos);
            return pos;
        }
        
        // Strategy 3: Any location + respect buffer (ignore valid location check)
        System.out.println("[VictoryMod] Dungeon_" + color + " relaxing valid location constraint");
        pos = findDungeonLocation(level, rand, spawnX, spawnZ, minRadius, maxRadius, bufferDistance, false, true);
        if (pos != null) {
            placeStructure(level, "dungeon_" + color, pos);
            return pos;
        }
        
        // Strategy 4: Any location, any radius (ignore valid location and buffer)
        System.out.println("[VictoryMod] Dungeon_" + color + " relaxing radius and proximity constraints");
        pos = findDungeonLocation(level, rand, spawnX, spawnZ, minRadius, maxRadius, 0, false, false);
        if (pos != null) {
            placeStructure(level, "dungeon_" + color, pos);
            return pos;
        }
        
        // Strategy 5: Force placement at a random location near spawn
        System.out.println("[VictoryMod] Dungeon_" + color + " forcing placement near spawn");
        int forceX = spawnX + rand.nextInt(400) - 200;
        int forceZ = spawnZ + rand.nextInt(400) - 200;
        int forceY = getGroundY(level, forceX, forceZ);
        BlockPos forcePos = new BlockPos(forceX, forceY, forceZ);
        placeStructure(level, "dungeon_" + color, forcePos);
        return forcePos;
    }

    /**
     * Find a dungeon location with configurable constraints.
     */
    private static BlockPos findDungeonLocation(ServerLevel level, Random rand, int spawnX, int spawnZ, 
                                               int minRadius, int maxRadius, int bufferDistance, 
                                               boolean requireValidLocation, boolean enforceRadius) {
        int attempts = 0;
        int maxAttempts = 100;
        
        while (attempts < maxAttempts) {
            int x, z;
            
            if (enforceRadius) {
                double angle = rand.nextDouble() * Math.PI * 2.0;
                int radius = minRadius + rand.nextInt(maxRadius - minRadius + 1);
                x = spawnX + (int) Math.round(radius * Math.cos(angle));
                z = spawnZ + (int) Math.round(radius * Math.sin(angle));
            } else {
                // Any location within a larger search area
                x = spawnX + rand.nextInt(maxRadius * 2) - maxRadius;
                z = spawnZ + rand.nextInt(maxRadius * 2) - maxRadius;
            }
            
            int y = getGroundY(level, x, z);
            BlockPos candidatePos = new BlockPos(x, y, z);
            
            boolean locationValid = !requireValidLocation || isValidSpawnLocation(level, candidatePos);
            boolean bufferOk = bufferDistance == 0 || !overlapsWithExisting(candidatePos, bufferDistance);
            
            if (locationValid && bufferOk) {
                return candidatePos;
            }
            
            attempts++;
        }
        
        return null;
    }

    private static int getGroundY(ServerLevel level, int x, int z) {
        // Use WORLD_SURFACE_WG in world spawn phase to avoid the 0-y bug from premature data.
        int y = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z);

        if (y <= level.getMinBuildHeight() + 1) {
            // fallback scan in case heightmap is not populated yet
            for (int scanY = level.getMaxBuildHeight() - 1; scanY > level.getMinBuildHeight(); scanY--) {
                if (!level.isEmptyBlock(new BlockPos(x, scanY, z))) {
                    y = scanY + 1;
                    break;
                }
            }
        }

        y = Math.max(y, level.getMinBuildHeight() + 1);
        y = Math.min(y, level.getMaxBuildHeight() - 1);
        return y;
    }

    /**
     * Finds a nearby valid spawn location within a search radius.
     * Searches in expanding circles around the center point.
     */
    private static BlockPos findNearbyValidLocation(ServerLevel level, int centerX, int centerZ, int searchRadius) {
        Random rand = new Random();
        
        // Try random locations in expanding circles
        for (int radius = 10; radius <= searchRadius; radius += 10) {
            for (int attempt = 0; attempt < 20; attempt++) {
                double angle = rand.nextDouble() * Math.PI * 2.0;
                int x = centerX + (int) Math.round(radius * Math.cos(angle));
                int z = centerZ + (int) Math.round(radius * Math.sin(angle));
                int y = getGroundY(level, x, z);
                
                BlockPos candidatePos = new BlockPos(x, y, z);
                if (isValidSpawnLocation(level, candidatePos)) {
                    return candidatePos;
                }
            }
        }
        
        return null;
    }

    /**
     * Checks if a location is valid for structure spawning.
     * Valid locations must be on solid land (not water or air-like blocks).
     */
    private static boolean isValidSpawnLocation(ServerLevel level, BlockPos pos) {
        // Check if the block below is solid (not water, not air, not leaves/trees)
        BlockPos belowPos = pos.below();
        net.minecraft.world.level.block.Block blockBelow = level.getBlockState(belowPos).getBlock();
        
        // Reject water blocks
        if (blockBelow instanceof net.minecraft.world.level.block.LiquidBlock) {
            return false;
        }
        
        // Reject leaves and tree-related blocks
        if (blockBelow instanceof net.minecraft.world.level.block.LeavesBlock) {
            return false;
        }
        
        // Reject air and void blocks
        if (blockBelow == Blocks.AIR || blockBelow == Blocks.VOID_AIR || blockBelow == Blocks.CAVE_AIR) {
            return false;
        }
        
        // Reject grass, seagrass, and similar non-solid vegetation
        String blockName = blockBelow.getName().getString();
        if (blockName.contains("grass") || blockName.contains("seagrass") || 
            blockName.contains("flower") || blockName.contains("mushroom") ||
            blockName.contains("vine") || blockName.contains("kelp")) {
            return false;
        }
        
        // Additional check: ensure it's not a liquid at the spawn position either
        if (level.getBlockState(pos).getBlock() instanceof net.minecraft.world.level.block.LiquidBlock) {
            return false;
        }
        
        return true;
    }

    /**
     * Checks if a position overlaps with any previously placed structure.
     * Considers the buffer distance around each placed structure.
     */
    private static boolean overlapsWithExisting(BlockPos pos, int bufferDistance) {
        for (BlockPos placedPos : placedStructures) {
            // Calculate horizontal distance (ignoring Y)
            int dx = pos.getX() - placedPos.getX();
            int dz = pos.getZ() - placedPos.getZ();
            double horizontalDistance = Math.sqrt(dx * dx + dz * dz);
            
            if (horizontalDistance < bufferDistance) {
                return true;
            }
        }
        return false;
    }

    private static void placeStructure(ServerLevel level, String name, BlockPos pos) {
        ResourceLocation templateId = ResourceLocation.tryParse("victorymod:" + name);
        if (templateId == null) {
            System.err.println("[VictoryMod] invalid structure id: " + name);
            return;
        }

        StructureTemplate template = level.getStructureManager().getOrCreate(templateId);

        if (template == null) {
            System.err.println("[VictoryMod] structure not found: " + name + " (id=" + templateId + ")");
            return;
        }

        boolean placed = template.placeInWorld(level, pos, pos, new StructurePlaceSettings(), level.random, 3);
        if (!placed) {
            System.err.println("[VictoryMod] failed to place structure " + name + " at " + pos);
        } else {
            System.out.println("[VictoryMod] placed " + name + " at " + pos);
        }
    }
}