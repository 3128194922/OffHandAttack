package com.example.offhandattack.util;

import com.example.offhandattack.ModTags;
import net.minecraft.world.item.ItemStack;

public class OffHandUtils {
    public static boolean isDuelItem(ItemStack stack) {
        if (stack.isEmpty()) return false;
        
        // Check Tag only
        return stack.is(ModTags.IS_DUEL);
    }
}
