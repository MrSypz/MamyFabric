package com.sypztep.mamy.client.screen;

import com.sypztep.mamy.common.component.living.LivingLevelComponent;
import com.sypztep.mamy.common.init.ModEntityComponents;
import com.sypztep.mamy.common.init.ModPassiveAbilities;
import com.sypztep.mamy.common.system.passive.PassiveAbility;
import com.sypztep.mamy.common.system.passive.PassiveAbilityManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;

public class PassiveAbilityScreen extends Screen {
    private final LivingLevelComponent playerStats;
    private final PassiveAbilityManager abilityManager;
    private PassiveAbility selectedAbility;
    private int scrollOffset = 0;

    public PassiveAbilityScreen(MinecraftClient client) {
        super(Text.literal("Passive Abilities"));
        assert client.player != null;
        this.playerStats = ModEntityComponents.LIVINGLEVEL.get(client.player);
        this.abilityManager = playerStats.getPassiveAbilityManager();
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        int centerX = this.width / 2;
        int centerY = this.height / 2;

        // Title
        context.drawCenteredTextWithShadow(textRenderer, title, centerX, 20, 0xFFFFFF);

        // Ability list
        renderAbilityList(context, 50, 50, 300, this.height - 100, mouseX, mouseY);

        // Selected ability details
        if (selectedAbility != null) {
            renderAbilityDetails(context, 370, 50, this.width - 420, this.height - 100);
        }


    }

    private void renderAbilityList(DrawContext context, int x, int y, int width, int height, int mouseX, int mouseY) {
        // Background
        context.fill(x, y, x + width, y + height, 0x80000000);

        // Header
        context.drawTextWithShadow(textRenderer, Text.literal("All Passive Abilities").formatted(Formatting.GOLD), x + 10, y + 10, 0xFFFFFF);

        List<PassiveAbility> abilities = ModPassiveAbilities.getAbilitiesOrderedByLevel();
        int currentY = y + 30;
        int itemHeight = 25;

        for (int i = scrollOffset; i < abilities.size() && currentY < y + height - itemHeight; i++) {
            PassiveAbility ability = abilities.get(i);

            boolean unlocked = abilityManager != null && abilityManager.isUnlocked(ability);
            boolean active = abilityManager != null && abilityManager.isActive(ability);
            boolean meetsRequirements = ability.meetsRequirements(client.player);
            boolean isHovered = mouseX >= x && mouseX <= x + width && mouseY >= currentY && mouseY <= currentY + itemHeight;
            boolean isSelected = ability == selectedAbility;

            // Background for item
            int itemBg = isSelected ? 0xFF444444 : (isHovered ? 0xFF333333 : 0xFF222222);
            context.fill(x + 5, currentY, x + width - 5, currentY + itemHeight, itemBg);

            // Status indicator
            String statusIcon;
            Formatting color;
            if (active) {
                statusIcon = "✓";
                color = Formatting.GREEN;
            } else if (unlocked) {
                statusIcon = "⚠";
                color = Formatting.YELLOW;
            } else if (meetsRequirements) {
                statusIcon = "!";
                color = Formatting.AQUA;
            } else {
                statusIcon = "✗";
                color = Formatting.GRAY;
            }

            // Render ability name
            Text abilityName = Text.literal(statusIcon + " " + ability.getDisplayName().getString()).formatted(color);
            context.drawTextWithShadow(textRenderer, abilityName, x + 10, currentY + 5, 0xFFFFFF);

            // Requirements summary
            String reqSummary = getRequirementSummary(ability);
            context.drawTextWithShadow(textRenderer, Text.literal(reqSummary).formatted(Formatting.DARK_GRAY), x + 10, currentY + 15, 0xFFFFFF);

            currentY += itemHeight + 2;
        }
    }

    private void renderAbilityDetails(DrawContext context, int x, int y, int width, int height) {
        // Background
        context.fill(x, y, x + width, y + height, 0x80000000);

        if (selectedAbility == null) return;

        int currentY = y + 10;

        // Ability name
        context.drawTextWithShadow(textRenderer, selectedAbility.getDisplayName(), x + 10, currentY, 0xFFFFFF);
        currentY += textRenderer.fontHeight + 10;

        // Status
        boolean unlocked = abilityManager != null && abilityManager.isUnlocked(selectedAbility);
        boolean active = abilityManager != null && abilityManager.isActive(selectedAbility);

        String status = active ? "§a✓ Active" : (unlocked ? "§e⚠ Unlocked but Inactive" : "§c✗ Locked");
        context.drawTextWithShadow(textRenderer, Text.literal("Status: " + status), x + 10, currentY, 0xFFFFFF);
        currentY += textRenderer.fontHeight + 10;

        // Tooltip details
        List<Text> tooltip = selectedAbility.getTooltip(client.player);
        for (Text line : tooltip) {
            if (currentY >= y + height - textRenderer.fontHeight) break;
            context.drawTextWithShadow(textRenderer, line, x + 10, currentY, 0xFFFFFF);
            currentY += textRenderer.fontHeight + 2;
        }
    }

    private String getRequirementSummary(PassiveAbility ability) {
        StringBuilder summary = new StringBuilder();
        var requirements = ability.getRequirements();

        for (var entry : requirements.entrySet()) {
            if (!summary.isEmpty()) summary.append(", ");
            summary.append(entry.getKey().getAka()).append(" ").append(entry.getValue());
        }

        return summary.toString();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (mouseX >= 50 && mouseX <= 350 && mouseY >= 80) {
            List<PassiveAbility> abilities = ModPassiveAbilities.getAbilitiesOrderedByLevel();
            int itemHeight = 27;
            int clickedIndex = (int) ((mouseY - 80) / itemHeight) + scrollOffset;

            if (clickedIndex >= 0 && clickedIndex < abilities.size()) {
                selectedAbility = abilities.get(clickedIndex);
                return true;
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (mouseX >= 50 && mouseX <= 350) {
            int maxScroll = Math.max(0, ModPassiveAbilities.getAllAbilities().size() - 10);
            scrollOffset = Math.max(0, Math.min(maxScroll, scrollOffset - (int) verticalAmount));
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}