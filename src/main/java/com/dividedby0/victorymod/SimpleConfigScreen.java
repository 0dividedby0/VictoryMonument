package com.dividedby0.victorymod;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import com.dividedby0.victorymod.config.JSON5ConfigManager;

/**
 * Simple config screen for Victory Mod settings.
 * Displays input fields for each configuration parameter.
 */
public class SimpleConfigScreen extends Screen {
    private final Screen previousScreen;
    private final JSON5ConfigManager configManager;
    private EditBox minRadiusInput;
    private EditBox maxRadiusInput;
    private EditBox bufferDistanceInput;
    
    public SimpleConfigScreen(Screen previousScreen, JSON5ConfigManager configManager) {
        super(Component.literal("Victory Mod Configuration"));
        this.previousScreen = previousScreen;
        this.configManager = configManager;
    }
    
    @Override
    protected void init() {
        int centerX = this.width / 2;
        int startY = 60;
        
        // Min radius input
        this.minRadiusInput = new EditBox(this.font, centerX - 100, startY, 200, 20, Component.literal("Min Radius"));
        this.minRadiusInput.setValue(String.valueOf(configManager.getInt("minDungeonRadius", 40)));
        this.addRenderableWidget(this.minRadiusInput);
        
        // Max radius input
        this.maxRadiusInput = new EditBox(this.font, centerX - 100, startY + 40, 200, 20, Component.literal("Max Radius"));
        this.maxRadiusInput.setValue(String.valueOf(configManager.getInt("maxDungeonRadius", 750)));
        this.addRenderableWidget(this.maxRadiusInput);
        
        // Buffer distance input
        this.bufferDistanceInput = new EditBox(this.font, centerX - 100, startY + 80, 200, 20, Component.literal("Buffer Distance"));
        this.bufferDistanceInput.setValue(String.valueOf(configManager.getInt("structureBufferDistance", 30)));
        this.addRenderableWidget(this.bufferDistanceInput);
        
        // Save button (left)
        this.addRenderableWidget(Button.builder(Component.literal("Save"), (btn) -> this.save())
            .bounds(centerX - 110, this.height - 30, 100, 20).build());
        
        // Back button (right)
        this.addRenderableWidget(Button.builder(Component.literal("Back"), (btn) -> this.onClose())
            .bounds(centerX + 10, this.height - 30, 100, 20).build());
    }
    
    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics);
        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 20, 0xFFFFFF);
        
        guiGraphics.drawString(this.font, "Min Dungeon Radius (10-500):", this.width / 2 - 190, 50, 0xAAAAAA);
        guiGraphics.drawString(this.font, "Max Dungeon Radius (50-1000):", this.width / 2 - 190, 90, 0xAAAAAA);
        guiGraphics.drawString(this.font, "Structure Buffer (5-200):", this.width / 2 - 190, 130, 0xAAAAAA);
        
        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }
    
    private void save() {
        try {
            int minRadius = Integer.parseInt(this.minRadiusInput.getValue());
            int maxRadius = Integer.parseInt(this.maxRadiusInput.getValue());
            int bufferDist = Integer.parseInt(this.bufferDistanceInput.getValue());
            
            // Validate ranges
            minRadius = Math.max(10, Math.min(500, minRadius));
            maxRadius = Math.max(50, Math.min(1000, maxRadius));
            bufferDist = Math.max(5, Math.min(200, bufferDist));
            
            // Save to config
            configManager.setInt("minDungeonRadius", minRadius);
            configManager.setInt("maxDungeonRadius", maxRadius);
            configManager.setInt("structureBufferDistance", bufferDist);
            configManager.saveConfig();
            
            this.onClose();
        } catch (NumberFormatException e) {
            // Show error - values will be reset on close
        }
    }
    
    @Override
    public void onClose() {
        this.minecraft.setScreen(this.previousScreen);
    }
    
    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
