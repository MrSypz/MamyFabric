package com.sypztep.mamy.client.screen.widget;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.MathHelper;

/**
 * Reusable scrolling behavior component that can be used by any widget
 * that needs smooth scrolling with animated scrollbar.
 */
public class ScrollBehavior {
    // Scroll state
    private double scrollAmount = 0;
    private double targetScrollAmount = 0;
    private int contentTotalHeight = 0;
    private int maxScroll = 0;

    // Scrollbar configuration
    private boolean enableScrollbar = true;
    private int scrollbarWidth = 6;
    private int scrollbarPadding = 2;
    private int minHandleSize = 20;

    // Animation state
    private float scrollbarHoverAnimation = 0.0f;
    private boolean isDragging = false;

    // Animation constants
    private static final float HOVER_ANIMATION_SPEED = 2f;
    private static final float FADE_ANIMATION_SPEED = 0.5f;
    private static final float SCROLL_LERP_SPEED = 0.3f;
    private static final double SCROLL_SENSITIVITY = 20.0;

    // Bounds (set by parent widget)
    private int x, y, width, height;

    public ScrollBehavior() {
    }

    /**
     * Update the bounds of the scrollable area
     */
    public void setBounds(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        updateMaxScroll();
    }

    /**
     * Set the total height of the content that needs to be scrolled
     */
    public void setContentHeight(int contentHeight) {
        this.contentTotalHeight = contentHeight;
        updateMaxScroll();
    }

    /**
     * Update scroll behavior and render scrollbar if needed
     */
    public void update(DrawContext context, int mouseX, int mouseY, float delta) {
        updateMaxScroll();
        updateScrolling(delta);
        updateScrollbarAnimation(mouseX, mouseY, delta);

        if (enableScrollbar && maxScroll > 0) {
            renderScrollbar(context, mouseX, mouseY);
        }
    }

    /**
     * Handle mouse scrolling input
     */
    public boolean handleScroll(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (maxScroll > 0 && isMouseInScrollArea(mouseX, mouseY)) {
            targetScrollAmount -= verticalAmount * SCROLL_SENSITIVITY;
            targetScrollAmount = MathHelper.clamp(targetScrollAmount, 0, maxScroll);
            return true;
        }
        return false;
    }

    /**
     * Handle mouse clicking
     */
    public boolean handleMouseClick(double mouseX, double mouseY, int button) {
        if (button == 0 && isScrollbarClicked(mouseX, mouseY)) {
            isDragging = true;
            updateScrollFromMousePosition(mouseY);
            return true;
        }
        return false;
    }

    /**
     * Handle mouse dragging
     */
    public boolean handleMouseDrag(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (isDragging && button == 0) {
            updateScrollFromMousePosition(mouseY);
            return true;
        }
        return false;
    }

    /**
     * Handle mouse release
     */
    public void handleMouseRelease(double mouseX, double mouseY, int button) {
        if (button == 0) {
            isDragging = false;
        }
    }

    /**
     * Enable or disable scissor clipping for content rendering
     */
    public void enableScissor(DrawContext context) {
        context.enableScissor(x, y, x + getContentWidth(), y + height);
    }

    public void disableScissor(DrawContext context) {
        context.disableScissor();
    }

    /**
     * Get the current scroll offset for content positioning
     */
    public int getScrollOffset() {
        return (int) scrollAmount;
    }

    /**
     * Get the content width (accounting for scrollbar)
     */
    public int getContentWidth() {
        return enableScrollbar && maxScroll > 0 ?
                width - scrollbarWidth - scrollbarPadding : width;
    }

    // Private helper methods

    private void updateMaxScroll() {
        maxScroll = Math.max(0, contentTotalHeight - height);
        scrollAmount = MathHelper.clamp(scrollAmount, 0, maxScroll);
        targetScrollAmount = MathHelper.clamp(targetScrollAmount, 0, maxScroll);
    }

    private void updateScrolling(float delta) {
        if (Math.abs(scrollAmount - targetScrollAmount) > 0.1) {
            // Frame-rate independent smooth scrolling
            float lerpFactor = 1.0f - (float)Math.exp(-SCROLL_LERP_SPEED * delta);
            scrollAmount = MathHelper.lerp(lerpFactor, scrollAmount, targetScrollAmount);
        } else {
            scrollAmount = targetScrollAmount;
        }
        scrollAmount = MathHelper.clamp(scrollAmount, 0, maxScroll);
    }

    private void updateScrollbarAnimation(int mouseX, int mouseY, float delta) {
        if (enableScrollbar && maxScroll > 0) {
            boolean scrollbarHovered = isScrollbarHovered(mouseX, mouseY);

            float targetAnimation = (scrollbarHovered || isDragging) ? 1.0f : 0.0f;

            // Frame-rate independent animation
            if (scrollbarHoverAnimation < targetAnimation) {
                scrollbarHoverAnimation = Math.min(1.0f,
                        scrollbarHoverAnimation + HOVER_ANIMATION_SPEED * delta);
            } else if (scrollbarHoverAnimation > targetAnimation) {
                scrollbarHoverAnimation = Math.max(0.0f,
                        scrollbarHoverAnimation - FADE_ANIMATION_SPEED * delta);
            }
        }
    }

    private void renderScrollbar(DrawContext context, int mouseX, int mouseY) {
        if (maxScroll <= 0) return;

        int scrollbarX = x + width - scrollbarWidth - scrollbarPadding;
        int scrollbarY = y;
        int scrollbarHeight = height;

        // Background with animation
        int bgAlpha = 25 + (int)(55 * scrollbarHoverAnimation);
        context.fill(scrollbarX, scrollbarY, scrollbarX + scrollbarWidth,
                scrollbarY + scrollbarHeight, (bgAlpha << 24));

        // Handle
        int handleHeight = Math.max(minHandleSize, scrollbarHeight * height / contentTotalHeight);
        int handleY = scrollbarY + (int)((scrollbarHeight - handleHeight) * scrollAmount / maxScroll);

        // Animate handle size when hovered
        int handleExpansion = (int)scrollbarHoverAnimation;
        int animatedScrollbarX = scrollbarX - handleExpansion;
        int animatedHandleHeight = handleHeight + handleExpansion * 2;

        // Colors with smooth transitions
        int baseAlpha = 120 + (int)(135 * scrollbarHoverAnimation);
        int handleBgColor = (baseAlpha << 24) | 0x666666;
        int handleFgColor = ColorHelper.Argb.lerp( scrollbarHoverAnimation,0xFFAAAAAA, 0xFFFFFFFF);

        // Glow effect when hovered
        if (scrollbarHoverAnimation > 0.1f) {
            int glowSize = (int)(4 * scrollbarHoverAnimation);
            int glowAlpha = (int)(40 * scrollbarHoverAnimation);
            int glowColor = (glowAlpha << 24) | 0xFFFFFF;

            context.fill(animatedScrollbarX - glowSize, handleY - glowSize,
                    animatedScrollbarX + scrollbarWidth + glowSize,
                    handleY + animatedHandleHeight + glowSize, glowColor);
        }

        // Handle background
        context.fill(animatedScrollbarX, handleY,
                animatedScrollbarX + scrollbarWidth, handleY + animatedHandleHeight,
                handleBgColor);

        // Handle foreground with rounded effect
        context.fill(animatedScrollbarX + 1, handleY + 1,
                animatedScrollbarX + scrollbarWidth - 1, handleY + animatedHandleHeight - 1,
                handleFgColor);

        // Grip lines when highly hovered
        if (scrollbarHoverAnimation > 0.5f) {
            int lineY = handleY + animatedHandleHeight / 2 - 3;
            int lineAlpha = (int)(255 * ((scrollbarHoverAnimation - 0.5f) * 2));
            int lineColor = (lineAlpha << 24) | 0x999999;

            for (int i = 0; i < 3; i++) {
                context.fill(animatedScrollbarX + 2, lineY + (i * 3),
                        animatedScrollbarX + scrollbarWidth - 2, lineY + (i * 3) + 1,
                        lineColor);
            }
        }
    }

    private boolean isMouseInScrollArea(double mouseX, double mouseY) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }

    private boolean isScrollbarHovered(int mouseX, int mouseY) {
        if (!enableScrollbar || maxScroll <= 0) return false;

        int scrollbarX = x + width - scrollbarWidth - scrollbarPadding;
        int scrollbarY = y;
        int scrollbarHeight = height;

        return mouseX >= scrollbarX && mouseX <= scrollbarX + scrollbarWidth &&
                mouseY >= scrollbarY && mouseY <= scrollbarY + scrollbarHeight;
    }

    private boolean isScrollbarClicked(double mouseX, double mouseY) {
        return isScrollbarHovered((int)mouseX, (int)mouseY);
    }

    private void updateScrollFromMousePosition(double mouseY) {
        int scrollbarY = y;
        int scrollbarHeight = height;

        double scrollRatio = (mouseY - scrollbarY) / (double)scrollbarHeight;
        scrollAmount = scrollRatio * maxScroll;
        targetScrollAmount = scrollAmount;
        scrollAmount = MathHelper.clamp(scrollAmount, 0, maxScroll);
        targetScrollAmount = MathHelper.clamp(targetScrollAmount, 0, maxScroll);
    }

    // Configuration methods
    public ScrollBehavior setScrollbarWidth(int width) {
        this.scrollbarWidth = width;
        return this;
    }

    public ScrollBehavior setScrollbarPadding(int padding) {
        this.scrollbarPadding = padding;
        return this;
    }

    public ScrollBehavior setMinHandleSize(int size) {
        this.minHandleSize = size;
        return this;
    }

    public ScrollBehavior setScrollbarEnabled(boolean enabled) {
        this.enableScrollbar = enabled;
        return this;
    }

    // Getters
    public double getScrollAmount() { return scrollAmount; }
    public int getMaxScroll() { return maxScroll; }
    public boolean isScrolling() { return maxScroll > 0; }
    public boolean isDragging() { return isDragging; }
    public float getScrollbarAnimation() { return scrollbarHoverAnimation; }

    /**
     * Scroll to a specific position (0.0 = top, 1.0 = bottom)
     */
    public void scrollToPosition(double position) {
        position = MathHelper.clamp(position, 0.0, 1.0);
        targetScrollAmount = position * maxScroll;
    }

    /**
     * Scroll to show a specific item at the given index
     */
    public void scrollToItem(int itemIndex, int itemHeight) {
        if (itemIndex < 0) return;

        int itemY = itemIndex * itemHeight;
        int visibleTop = (int)scrollAmount;
        int visibleBottom = visibleTop + height;

        if (itemY < visibleTop) {
            // Item is above visible area, scroll up
            targetScrollAmount = itemY;
        } else if (itemY + itemHeight > visibleBottom) {
            // Item is below visible area, scroll down
            targetScrollAmount = itemY + itemHeight - height;
        }

        targetScrollAmount = MathHelper.clamp(targetScrollAmount, 0, maxScroll);
    }
    public void resetScroll() {
        targetScrollAmount = 0;
    }
}