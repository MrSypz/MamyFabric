package com.sypztep.mamy.common.init;

import com.sypztep.mamy.Mamy;
import com.sypztep.mamy.common.item.GreatSword;
import net.minecraft.item.Item;
import net.minecraft.item.ToolMaterials;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public class ModItems {
    public static Item GREAT_SWORD;
    public static void init() {
        GREAT_SWORD = registeritem("great_sword", new GreatSword(ToolMaterials.IRON,
                new Item.Settings().attributeModifiers(GreatSword.createAttributeModifiers())));
    }

    public static <T extends Item> T registeritem(String name, T item) {
        Registry.register(Registries.ITEM, Mamy.id(name), item);
        return item;
    }
}
