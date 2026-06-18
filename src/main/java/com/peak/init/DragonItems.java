package com.peak.init;

import com.peak.Main;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;

public class DragonItems {
    public static final Item EYE_OF_GOD = new EyeOfGodItem(new Item.Settings().maxCount(16).rarity(Rarity.EPIC));

    public static void register() {
        Registry.register(Registries.ITEM, Identifier.of(Main.MODID, "eye_of_god"), EYE_OF_GOD);
    }
}
