package com.dividedby0.victorymod;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import com.dividedby0.victorymod.config.JSON5ConfigManager;

import java.util.function.IntConsumer;

public class SimpleConfigScreen extends Screen {
    private final Screen previousScreen;
    private final JSON5ConfigManager configManager;
    private int minRadius;
    private int maxRadius;
    private int bufferDistance;
    
    public SimpleConfigScreen(Screen previousScreen, JSON5ConfigManager configManager) {
        super(Component.literal("Victory Mod Configuration"));
        this.previousScreen = previousScreen;
        this.configManager = configManager;
    }
    
    @Override
    protected void init() {
        this.clearWidgets();

        minRadius = configManager.getInt("minDungeonRadius", 40);
        maxRadius = configManager.getInt("maxDungeonRadius", 750);
        bufferDistance = configManager.getInt("structureBufferDistance", 30);

        int centerX = this.width / 2;
        int y = 52;

        addIntSlider(centerX - 75, y, "Min Dungeon Radius", 10, 500, minRadius, value -> minRadius = value);
        y += 24;
        addIntSlider(centerX - 75, y, "Max Dungeon Radius", 50, 1000, maxRadius, value -> maxRadius = value);
        y += 24;
        addIntSlider(centerX - 75, y, "Structure Buffer", 5, 200, bufferDistance, value -> bufferDistance = value);

        int buttonY = this.height - 40;
        this.addRenderableWidget(Button.builder(Component.literal("Save"), (btn) -> this.save())
            .bounds(centerX - 110, buttonY, 100, 20).build());
        this.addRenderableWidget(Button.builder(Component.literal("Back"), (btn) -> this.onClose())
            .bounds(centerX + 10, buttonY, 100, 20).build());
    }
    
    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics);
        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 20, 0xFFFFFF);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }
    
    private void save() {
        configManager.setInt("minDungeonRadius", Mth.clamp(minRadius, 10, 500));
        configManager.setInt("maxDungeonRadius", Mth.clamp(maxRadius, 50, 1000));
        configManager.setInt("structureBufferDistance", Mth.clamp(bufferDistance, 5, 200));
        configManager.saveConfig();
        this.onClose();
    }

    private IntSlider addIntSlider(int x, int y, String title, int min, int max, int value, IntConsumer onChange) {
        return this.addRenderableWidget(new IntSlider(x, y, 150, 20, title, min, max, value, onChange));
    }

    private static class IntSlider extends AbstractSliderButton {
        private final String title;
        private final int min;
        private final int max;
        private final IntConsumer onChange;

        IntSlider(int x, int y, int width, int height, String title, int min, int max, int value, IntConsumer onChange) {
            super(x, y, width, height, Component.empty(), toSlider(value, min, max));
            this.title = title;
            this.min = min;
            this.max = max;
            this.onChange = onChange;
            this.updateMessage();
        }

        @Override
        protected void updateMessage() {
            this.setMessage(Component.literal(this.title + ": " + this.toValue()));
        }

        @Override
        protected void applyValue() {
            this.onChange.accept(this.toValue());
        }

        private int toValue() {
            return Mth.clamp((int) Math.round(this.min + (this.max - this.min) * this.value), this.min, this.max);
        }

        private static double toSlider(int value, int min, int max) {
            if (max <= min) {
                return 0.0;
            }
            return Mth.clamp((value - (double) min) / (double) (max - min), 0.0, 1.0);
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
