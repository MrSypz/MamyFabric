package com.sypztep.mamy.client.util;

import com.mojang.blaze3d.systems.RenderSystem;
import org.lwjgl.opengl.GL30;

/**
 * Enum for common blend modes
 */
public enum BlendMode {
    // === NORMAL GROUP ===
    NORMAL,                    // Standard alpha blending
    DISSOLVE,                  // Random pixel dissolve effect

    // === DARKEN GROUP ===
    DARKEN,                    // Keep darker pixels
    MULTIPLY,                  // Multiply blend (darkens)
    COLOR_BURN,                // Burn effect (high contrast darken)
    LINEAR_BURN,               // Linear burn (additive darken)
    DARKER_COLOR,              // Choose darker color overall

    // === LIGHTEN GROUP ===
    LIGHTEN,                   // Keep lighter pixels
    SCREEN,                    // Screen blend (brightens)
    COLOR_DODGE,               // Dodge effect (high contrast brighten)
    LINEAR_DODGE,              // Linear dodge (same as additive)
    ADDITIVE,                  // Additive blend (very bright)
    LIGHTER_COLOR,             // Choose lighter color overall

    // === CONTRAST GROUP ===
    OVERLAY,                   // Overlay blend (contrast)
    SOFT_LIGHT,                // Soft light (gentle contrast)
    HARD_LIGHT,                // Hard light (strong contrast)
    VIVID_LIGHT,               // Vivid light (extreme contrast)
    LINEAR_LIGHT,              // Linear light
    PIN_LIGHT,                 // Pin light
    HARD_MIX,                  // Hard mix (posterize effect)

    // === COMPARATIVE GROUP ===
    DIFFERENCE,                // Absolute difference
    EXCLUSION,                 // Exclusion blend
    SUBTRACT,                  // Subtract blend
    DIVIDE,                    // Divide blend

    // === COLOR GROUP ===
    HUE,                       // Change hue only
    SATURATION,                // Change saturation only
    COLOR,                     // Change hue and saturation
    LUMINOSITY,                // Change brightness only

    // === CUSTOM GAMING EFFECTS ===
    MULTIPLY_SCREEN_HYBRID;
    /**
     * Comprehensive blend mode application
     */

    // ===== BLEND MODE UTILITIES =====

    public static void applyBlendMode(BlendMode mode) {
        switch (mode) {
            // === NORMAL GROUP ===
            case NORMAL:
                RenderSystem.blendFuncSeparate(
                        GL30.GL_SRC_ALPHA, GL30.GL_ONE_MINUS_SRC_ALPHA,
                        GL30.GL_ONE, GL30.GL_ZERO
                );
                break;

            case DISSOLVE:
                // Approximation using dithering effect
                RenderSystem.blendFuncSeparate(
                        GL30.GL_SRC_ALPHA, GL30.GL_ONE_MINUS_SRC_ALPHA,
                        GL30.GL_ONE, GL30.GL_ZERO
                );
                break;

            // === DARKEN GROUP ===
            case DARKEN:
                RenderSystem.blendFunc(GL30.GL_ONE, GL30.GL_ONE);
                // Note: True darken needs shader support, this is approximation
                break;

            case MULTIPLY:
                RenderSystem.blendFuncSeparate(
                        GL30.GL_DST_COLOR, GL30.GL_ZERO,
                        GL30.GL_ONE, GL30.GL_ZERO
                );
                break;

            case COLOR_BURN:
                RenderSystem.blendFuncSeparate(
                        GL30.GL_ZERO, GL30.GL_ONE_MINUS_SRC_COLOR,
                        GL30.GL_ONE, GL30.GL_ZERO
                );
                break;

            case LINEAR_BURN:
                RenderSystem.blendFuncSeparate(
                        GL30.GL_ONE, GL30.GL_ONE,
                        GL30.GL_ONE, GL30.GL_ZERO
                );
                break;

            // === LIGHTEN GROUP ===
            case LIGHTEN:
                RenderSystem.blendFunc(GL30.GL_SRC_ALPHA, GL30.GL_ONE);
                break;

            case SCREEN:
                RenderSystem.blendFuncSeparate(
                        GL30.GL_ONE_MINUS_DST_COLOR, GL30.GL_ONE,
                        GL30.GL_ONE, GL30.GL_ONE_MINUS_SRC_ALPHA
                );
                break;

            case COLOR_DODGE:
                RenderSystem.blendFuncSeparate(
                        GL30.GL_DST_COLOR, GL30.GL_ONE,
                        GL30.GL_ONE, GL30.GL_ONE
                );
                break;

            case LINEAR_DODGE:
            case ADDITIVE:
                RenderSystem.blendFuncSeparate(
                        GL30.GL_SRC_ALPHA, GL30.GL_ONE,
                        GL30.GL_ONE, GL30.GL_ONE
                );
                break;

            // === CONTRAST GROUP ===
            case OVERLAY:
                RenderSystem.blendFuncSeparate(
                        GL30.GL_DST_COLOR, GL30.GL_SRC_COLOR,
                        GL30.GL_ONE, GL30.GL_ZERO
                );
                break;

            case SOFT_LIGHT:
                RenderSystem.blendFuncSeparate(
                        GL30.GL_ONE_MINUS_DST_COLOR, GL30.GL_SRC_COLOR,
                        GL30.GL_ONE, GL30.GL_ZERO
                );
                break;

            case HARD_LIGHT:
                RenderSystem.blendFuncSeparate(
                        GL30.GL_DST_COLOR, GL30.GL_ONE_MINUS_SRC_COLOR,
                        GL30.GL_ONE, GL30.GL_ZERO
                );
                break;

            // === COMPARATIVE GROUP ===
            case DIFFERENCE:
                RenderSystem.blendFuncSeparate(
                        GL30.GL_ONE, GL30.GL_ONE,
                        GL30.GL_ONE, GL30.GL_ZERO
                );
                break;

            case EXCLUSION:
                RenderSystem.blendFuncSeparate(
                        GL30.GL_ONE_MINUS_DST_COLOR, GL30.GL_ONE_MINUS_SRC_COLOR,
                        GL30.GL_ONE, GL30.GL_ZERO
                );
                break;

            case SUBTRACT:
                RenderSystem.blendFuncSeparate(
                        GL30.GL_ZERO, GL30.GL_ONE_MINUS_SRC_COLOR,
                        GL30.GL_ONE, GL30.GL_ZERO
                );
                break;

            // === CUSTOM GAMING EFFECTS ===
            case MULTIPLY_SCREEN_HYBRID:
                RenderSystem.blendFuncSeparate(
                        GL30.GL_DST_COLOR, GL30.GL_ONE_MINUS_SRC_ALPHA,
                        GL30.GL_ONE, GL30.GL_ONE_MINUS_SRC_ALPHA
                );
                break;

            default:
                RenderSystem.blendFuncSeparate(
                        GL30.GL_SRC_ALPHA, GL30.GL_ONE_MINUS_SRC_ALPHA,
                        GL30.GL_ONE, GL30.GL_ZERO
                );
                break;
        }
    }
}