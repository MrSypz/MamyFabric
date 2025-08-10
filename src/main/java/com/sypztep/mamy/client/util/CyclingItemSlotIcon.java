package com.sypztep.mamy.client.util;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;

import java.util.ArrayList;
import java.util.List;

public final class CyclingItemSlotIcon {
    private int timer;
    private int currentIndex;
    private final int slotId;
    private List<ItemStack> itemStacks = new ArrayList<>();

    public CyclingItemSlotIcon(int slotId) {
        this.slotId = slotId;
    }

    public void updateTexture(List<ItemStack> itemStacks) {
        if (!this.itemStacks.equals(itemStacks)) {
            this.itemStacks = itemStacks;
            this.currentIndex = 0;
        }

        if (!this.itemStacks.isEmpty() && ++this.timer % 30 == 0) {
            this.currentIndex = (this.currentIndex + 1) % this.itemStacks.size();
        }
    }

    public void render(ScreenHandler screenHandler, DrawContext context, float delta, int x, int y) {
        Slot slot = screenHandler.getSlot(this.slotId);
        if (!this.itemStacks.isEmpty() && !slot.hasStack()) {
            boolean bl = this.itemStacks.size() > 1 && this.timer >= 30;
            float f = bl ? this.computeAlpha(delta) : 1.0F;
            if (f < 1.0F) {
                int i = Math.floorMod(this.currentIndex - 1, this.itemStacks.size());
                this.drawItemIcon(this.itemStacks.get(i), 1.0F - f, context, x, y);
            }

            this.drawItemIcon(this.itemStacks.get(this.currentIndex), f, context, x, y);
        }
    }

    private void drawItemIcon(ItemStack itemStack, float alpha, DrawContext context, int x, int y) {
        context.getMatrices().push();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, alpha);
        context.drawItem(itemStack,x,y);
        context.getMatrices().pop();
    }

    private float computeAlpha(float delta) {
        float f = (float)(this.timer % 30) + delta;
        return Math.min(f, 4.0F) / 4.0F;
    }
}