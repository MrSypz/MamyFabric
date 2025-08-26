package com.sypztep.mamy.client.event.tooltip;

import java.util.ArrayList;
import java.util.List;

import com.sypztep.mamy.Mamy;
import com.sypztep.mamy.common.util.NumberUtil;
//import com.sypztep.mamyvault.MamyAPI;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.text.MutableText;
import net.minecraft.util.Formatting;
import com.sypztep.mamy.common.data.ItemWeightEntry;

public final class ItemWeightTooltip implements ItemTooltipCallback {
    public static void register() {
        ItemTooltipCallback.EVENT.register(new ItemWeightTooltip());
    }

    public static final String WEIGHT_ICON = "\u0020";
    public static final String COIN_ICON = "\u0019";

//    private static List<MamyAPI.LightMarketItem> cachedMarketItems = new ArrayList<>();
    private static long lastMarketUpdate = 0;
    private static final long MARKET_CACHE_DURATION = 30000; // 30 seconds

    @Override
    public void getTooltip(ItemStack stack, Item.TooltipContext tooltipContext, TooltipType tooltipType, List<Text> lines) {
        double displayValue = ItemWeightEntry.getWeight(stack);

        MutableText weightLine = Text.empty();
        weightLine.append(ElementalTooltipHelper.createIconText(WEIGHT_ICON));
        weightLine.append(Text.literal(" Weight: ").formatted(Formatting.GRAY));
        weightLine.append(Text.literal(String.format("%,.2f", displayValue)).formatted(Formatting.YELLOW));
        weightLine.append(Text.literal(" LT").formatted(Formatting.DARK_GRAY));

        lines.add(weightLine);

        addMarketValueTooltip(stack, lines);
    }

    private void addMarketValueTooltip(ItemStack stack, List<Text> lines) {
        if (!Mamy.isMamyVaultLoaded) return;

//        updateMarketCache();
//        String itemKey = Registries.ITEM.getId(stack.getItem()).toString();

//        MamyAPI.LightMarketItem marketItem = findMarketItem(itemKey);
//        if (marketItem != null)
//            displayMarketValue(marketItem.currentSellPrice(), marketItem.priceMultiplier(), lines);
    }

    private void displayMarketValue(long price, double multiplier, List<Text> lines) {
        Formatting priceColor = multiplier > 1.0 ? Formatting.RED :
                multiplier < 1.0 ? Formatting.GREEN : Formatting.YELLOW;

        MutableText valueLine = Text.empty();
        valueLine.append(ElementalTooltipHelper.createIconText(COIN_ICON));
        valueLine.append(Text.literal(" Value: ").formatted(Formatting.GRAY));
        valueLine.append(Text.literal(NumberUtil.formatNumber(price)).formatted(priceColor));
        valueLine.append(Text.literal(" (" + String.format("%.1f%%", (multiplier - 1.0) * 100) + ")").formatted(Formatting.DARK_GRAY));

        lines.add(valueLine);
    }

//    private MamyAPI.LightMarketItem findMarketItem(String itemKey) {
//        return cachedMarketItems.stream()
//                .filter(item -> item.itemKey().equals(itemKey))
//                .findFirst()
//                .orElse(null);
//    }
//
//    private void updateMarketCache() {
//        long currentTime = System.currentTimeMillis();
//        if (currentTime - lastMarketUpdate > MARKET_CACHE_DURATION) {
//            lastMarketUpdate = currentTime;
//
//            MamyAPI.getMarketItemsLight().thenAccept(items -> cachedMarketItems = items)
//                    .exceptionally(throwable -> null);
//        }
//    }
}