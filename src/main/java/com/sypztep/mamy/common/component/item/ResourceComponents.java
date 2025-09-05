package com.sypztep.mamy.common.component.item;


import net.minecraft.item.Items;

public class ResourceComponents {
    public static final ResourceComponent POCKET_RESOURCE = new ResourceComponent.Builder()
            .resourceAmount(25.0F)
            .fastDrink()
            .usingConvertsTo(Items.GLASS_BOTTLE)
            .build();

    public static final ResourceComponent LESSER_RESOURCE = new ResourceComponent.Builder()
            .resourceAmount(50.0F)
            .fastDrink()
            .usingConvertsTo(Items.GLASS_BOTTLE)
            .build();

    public static final ResourceComponent STANDARD_RESOURCE = new ResourceComponent.Builder()
            .resourceAmount(100.0F)
            .normalDrink()
            .usingConvertsTo(Items.GLASS_BOTTLE)
            .build();

    public static final ResourceComponent GREATER_RESOURCE = new ResourceComponent.Builder()
            .resourceAmount(250.0F)
            .normalDrink()
            .usingConvertsTo(Items.GLASS_BOTTLE)
            .build();

    public static final ResourceComponent SUPERIOR_RESOURCE = new ResourceComponent.Builder()
            .resourceAmount(550.0F)
            .slowDrink()
            .usingConvertsTo(Items.GLASS_BOTTLE)
            .build();

    public static final ResourceComponent ULTIMATE_RESOURCE = new ResourceComponent.Builder()
            .resourceAmount(750.0F)
            .slowDrink()
            .usingConvertsTo(Items.GLASS_BOTTLE)
            .build();
}