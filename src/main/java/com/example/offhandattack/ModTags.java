package com.example.offhandattack;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

@SuppressWarnings("deprecation")
public class ModTags {
    public static final TagKey<Item> IS_HANDS = TagKey.create(Registries.ITEM, new ResourceLocation("offhandattack:is_hands"));
    public static final TagKey<Item> IS_DUEL = TagKey.create(Registries.ITEM, new ResourceLocation("offhandattack:is_duel"));
}
