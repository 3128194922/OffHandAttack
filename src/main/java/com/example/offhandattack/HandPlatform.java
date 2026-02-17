package com.example.offhandattack;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class HandPlatform {

    public static void setItemStackToSlot(Player playerIn, EquipmentSlot slotIn, ItemStack stack) {
        if (slotIn == EquipmentSlot.MAINHAND) {
            playerIn.getInventory().items.set(playerIn.getInventory().selected, stack);
            // Force attributes refresh?
            // playerIn.getAttributes().removeAttributeModifiers(playerIn.getInventory().getItem(playerIn.getInventory().selected).getAttributeModifiers(EquipmentSlot.MAINHAND));
            // playerIn.getAttributes().addTransientAttributeModifiers(stack.getAttributeModifiers(EquipmentSlot.MAINHAND));
            // Actually, attack() method calculates attributes dynamically from MainHand item.
            // But we need to ensure the game "sees" the item change.
            // setItemSlot already does this for mobs, but for players it updates inventory.
            // Let's try calling playerIn.setItemSlot to force updates if needed, but inventory set is usually enough for attack calculation.
            // However, attack() uses getAttribute(Attributes.ATTACK_DAMAGE).getValue() which might be cached or require a tick?
            // No, attributes are usually updated when item stack changes in slot.
            // But direct inventory set might NOT trigger attribute update immediately.
            // Let's use setItemSlot which handles attribute removal/addition.
            playerIn.setItemSlot(slotIn, stack);
        } else if (slotIn == EquipmentSlot.OFFHAND) {
            playerIn.setItemSlot(slotIn, stack);
        }
    }
}
