package com.sypztep.mamy.client.event.tooltip;

import java.util.List;

import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;
import net.minecraft.text.MutableText;
import net.minecraft.util.Formatting;
import com.sypztep.mamy.common.data.ItemWeightEntry;

public final class ItemWeightTooltip implements ItemTooltipCallback {
    public static void register() {
        ItemTooltipCallback.EVENT.register(new ItemWeightTooltip());
    }

    public static final String WEIGHT_ICON = "\u0020";

    @Override
    public void getTooltip(ItemStack stack, Item.TooltipContext tooltipContext, TooltipType tooltipType, List<Text> lines) {
        double displayValue = ItemWeightEntry.getWeight(stack);

        MutableText weightLine = Text.empty();
        weightLine.append(ElementalTooltipHelper.createIconText(WEIGHT_ICON));
        weightLine.append(Text.literal(" Weight: ").formatted(Formatting.GRAY));
        weightLine.append(Text.literal(String.format("%,.2f", displayValue)).formatted(Formatting.YELLOW));
        weightLine.append(Text.literal(" LT").formatted(Formatting.DARK_GRAY));

        lines.add(weightLine);
    }
}