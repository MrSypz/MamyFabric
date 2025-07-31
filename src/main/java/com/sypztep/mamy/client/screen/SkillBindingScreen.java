package com.sypztep.mamy.client.screen;

import com.sypztep.mamy.common.init.ModEntityComponents;
import com.sypztep.mamy.common.payload.BindSkillPayloadC2S;
import com.sypztep.mamy.common.system.skill.Skill;
import com.sypztep.mamy.common.system.skill.SkillRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;

public class SkillBindingScreen extends Screen {
    private static final int BACKGROUND_WIDTH = 256;
    private static final int BACKGROUND_HEIGHT = 200;
    private static final int SKILL_SLOT_SIZE = 24;
    private static final int SKILL_BUTTON_SIZE = 20;

    private final MinecraftClient client;
    private int backgroundX, backgroundY;

    // Skill data
    private List<Identifier> learnedSkills = new ArrayList<>();
    private Identifier[] boundSkills = new Identifier[8];
    private Identifier selectedSkill = null;

    // Slot positions
    private static final String[] SLOT_KEYS = {"Z", "X", "C", "V", "Shift+Z", "Shift+X", "Shift+C", "Shift+V"};

    public SkillBindingScreen(MinecraftClient client) {
        super(Text.literal("Skill Binding"));
        this.client = client;
        loadSkillData();
    }

    private void loadSkillData() {
        if (client.player == null) return;

        var classComponent = ModEntityComponents.PLAYERCLASS.get(client.player);
        learnedSkills = classComponent.getLearnedSkills();
        boundSkills = classComponent.getClassManager().getAllBoundSkills();
    }

    @Override
    protected void init() {
        super.init();

        backgroundX = (width - BACKGROUND_WIDTH) / 2;
        backgroundY = (height - BACKGROUND_HEIGHT) / 2;

        // Close button
        addDrawableChild(ButtonWidget.builder(Text.literal("Close"), button -> close())
                .dimensions(backgroundX + BACKGROUND_WIDTH - 50, backgroundY + BACKGROUND_HEIGHT - 25, 45, 20)
                .build());

        // Clear all button
        addDrawableChild(ButtonWidget.builder(Text.literal("Clear All"), button -> clearAllSkills())
                .dimensions(backgroundX + 5, backgroundY + BACKGROUND_HEIGHT - 25, 60, 20)
                .build());

        // Skill slot buttons (8 slots)
        for (int i = 0; i < 8; i++) {
            final int slot = i;
            int x = backgroundX + 20 + (i % 4) * 30;
            int y = backgroundY + 40 + (i / 4) * 30;

            addDrawableChild(ButtonWidget.builder(Text.literal(""), button -> onSkillSlotClick(slot))
                    .dimensions(x, y, SKILL_SLOT_SIZE, SKILL_SLOT_SIZE)
                    .build());
        }

        // Learned skills list (scrollable area)
        int startY = backgroundY + 110;
        for (int i = 0; i < learnedSkills.size(); i++) {
            final Identifier skillId = learnedSkills.get(i);
            int x = backgroundX + 10 + (i % 10) * 22;
            int y = startY + (i / 10) * 22;

            if (y < backgroundY + BACKGROUND_HEIGHT - 30) { // Only show if fits in screen
                addDrawableChild(ButtonWidget.builder(Text.literal(""), button -> onLearnedSkillClick(skillId))
                        .dimensions(x, y, SKILL_BUTTON_SIZE, SKILL_BUTTON_SIZE)
                        .build());
            }
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        context.fill(backgroundX, backgroundY, backgroundX + BACKGROUND_WIDTH, backgroundY + BACKGROUND_HEIGHT, 0x88000000);
        context.drawBorder(backgroundX, backgroundY, BACKGROUND_WIDTH, BACKGROUND_HEIGHT, 0xFFFFFFFF);

        // Title
        context.drawCenteredTextWithShadow(textRenderer, title, width / 2, backgroundY + 10, 0xFFFFFF);

        // Section headers
        context.drawTextWithShadow(textRenderer, Text.literal("Skill Slots"), backgroundX + 10, backgroundY + 25, 0xFFFFFF);
        context.drawTextWithShadow(textRenderer, Text.literal("Learned Skills"), backgroundX + 10, backgroundY + 95, 0xFFFFFF);

        // Render skill slots
        for (int i = 0; i < 8; i++) {
            int x = backgroundX + 20 + (i % 4) * 30;
            int y = backgroundY + 40 + (i / 4) * 30;

            // Slot background
            int color = (selectedSkill != null) ? 0xFF4CAF50 : 0xFF424242;
            context.fill(x, y, x + SKILL_SLOT_SIZE, y + SKILL_SLOT_SIZE, color);

            // Key label
            String keyText = SLOT_KEYS[i];
            context.drawTextWithShadow(textRenderer, Text.literal(keyText), x + 2, y - 10, 0xFFFFFF);

            // Bound skill
            if (boundSkills[i] != null) {
                Skill skill = SkillRegistry.getSkill(boundSkills[i]);
                if (skill != null) {
                    String skillName = skill.getName().substring(0, Math.min(2, skill.getName().length()));
                    context.drawCenteredTextWithShadow(textRenderer, Text.literal(skillName),
                            x + SKILL_SLOT_SIZE / 2, y + 8, 0xFFFFFF);
                }
            }
        }

        // Render learned skills
        int startY = backgroundY + 110;
        for (int i = 0; i < learnedSkills.size(); i++) {
            Identifier skillId = learnedSkills.get(i);
            int x = backgroundX + 10 + (i % 10) * 22;
            int y = startY + (i / 10) * 22;

            if (y < backgroundY + BACKGROUND_HEIGHT - 30) {
                // Skill background
                int color = skillId.equals(selectedSkill) ? 0xFF2196F3 : 0xFF616161;
                context.fill(x, y, x + SKILL_BUTTON_SIZE, y + SKILL_BUTTON_SIZE, color);

                // Skill name (abbreviated)
                Skill skill = SkillRegistry.getSkill(skillId);
                if (skill != null) {
                    String skillName = skill.getName().substring(0, Math.min(2, skill.getName().length()));
                    context.drawCenteredTextWithShadow(textRenderer, Text.literal(skillName),
                            x + SKILL_BUTTON_SIZE / 2, y + 6, 0xFFFFFF);
                }
            }
        }

        // Instructions
        String instruction = selectedSkill == null ?
                "Select a skill, then click a slot to bind" :
                "Click a slot to bind selected skill";
        context.drawCenteredTextWithShadow(textRenderer, Text.literal(instruction),
                width / 2, backgroundY + BACKGROUND_HEIGHT - 40, 0xFFFFFF);
        super.render(context, mouseX, mouseY, delta);

        // Render tooltips last (on top of everything)
        renderTooltips(context, mouseX, mouseY);
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
    }

    private void renderTooltips(DrawContext context, int mouseX, int mouseY) {
        // Skill slot tooltips
        for (int i = 0; i < 8; i++) {
            int x = backgroundX + 20 + (i % 4) * 30;
            int y = backgroundY + 40 + (i / 4) * 30;

            if (mouseX >= x && mouseX < x + SKILL_SLOT_SIZE && mouseY >= y && mouseY < y + SKILL_SLOT_SIZE) {
                List<Text> tooltip = new ArrayList<>();
                tooltip.add(Text.literal("Slot " + (i + 1) + " (" + SLOT_KEYS[i] + ")").formatted(Formatting.YELLOW));

                if (boundSkills[i] != null) {
                    Skill skill = SkillRegistry.getSkill(boundSkills[i]);
                    if (skill != null) {
                        tooltip.add(Text.literal(skill.getName()).formatted(Formatting.WHITE));
                        tooltip.add(Text.literal(skill.getDescription()).formatted(Formatting.GRAY));
                        tooltip.add(Text.literal("Right-click to unbind").formatted(Formatting.RED));
                    }
                } else {
                    tooltip.add(Text.literal("Empty slot").formatted(Formatting.GRAY));
                }

                context.drawTooltip(textRenderer, tooltip, mouseX, mouseY);
                break;
            }
        }

        // Learned skill tooltips
        int startY = backgroundY + 110;
        for (int i = 0; i < learnedSkills.size(); i++) {
            Identifier skillId = learnedSkills.get(i);
            int x = backgroundX + 10 + (i % 10) * 22;
            int y = startY + (i / 10) * 22;

            if (y < backgroundY + BACKGROUND_HEIGHT - 30 &&
                    mouseX >= x && mouseX < x + SKILL_BUTTON_SIZE && mouseY >= y && mouseY < y + SKILL_BUTTON_SIZE) {

                Skill skill = SkillRegistry.getSkill(skillId);
                if (skill != null) {
                    List<Text> tooltip = new ArrayList<>();
                    tooltip.add(Text.literal(skill.getName()).formatted(Formatting.YELLOW));
                    tooltip.add(Text.literal(skill.getDescription()).formatted(Formatting.WHITE));
                    tooltip.add(Text.literal("Cost: " + skill.getResourceCost()).formatted(Formatting.BLUE));
                    tooltip.add(Text.literal("Cooldown: " + (skill.getCooldown() / 20) + "s").formatted(Formatting.AQUA));

                    context.drawTooltip(textRenderer, tooltip, mouseX, mouseY);
                }
                break;
            }
        }
    }

    private void onSkillSlotClick(int slot) {
        if (selectedSkill != null) {
            // Bind selected skill to slot
            BindSkillPayloadC2S.send(slot, selectedSkill);
            boundSkills[slot] = selectedSkill;
            selectedSkill = null;

            if (client.player != null) {
                client.player.sendMessage(Text.literal("Skill bound to slot " + (slot + 1))
                        .formatted(Formatting.GREEN), false);
            }
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // Right-click to unbind skill slots
        if (button == 1) { // Right click
            for (int i = 0; i < 8; i++) {
                int x = backgroundX + 20 + (i % 4) * 30;
                int y = backgroundY + 40 + (i / 4) * 30;

                if (mouseX >= x && mouseX < x + SKILL_SLOT_SIZE && mouseY >= y && mouseY < y + SKILL_SLOT_SIZE) {
                    if (boundSkills[i] != null) {
                        BindSkillPayloadC2S.unbind(i);
                        boundSkills[i] = null;

                        if (client.player != null) {
                            client.player.sendMessage(Text.literal("Unbound skill from slot " + (i + 1))
                                    .formatted(Formatting.YELLOW), false);
                        }
                    }
                    return true;
                }
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void onLearnedSkillClick(Identifier skillId) {
        selectedSkill = skillId.equals(selectedSkill) ? null : skillId;
    }

    private void clearAllSkills() {
        for (int i = 0; i < 8; i++) {
            if (boundSkills[i] != null) {
                BindSkillPayloadC2S.unbind(i);
                boundSkills[i] = null;
            }
        }

        if (client.player != null) {
            client.player.sendMessage(Text.literal("Cleared all skill bindings")
                    .formatted(Formatting.YELLOW), false);
        }
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}