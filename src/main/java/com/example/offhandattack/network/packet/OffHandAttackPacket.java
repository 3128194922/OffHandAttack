package com.example.offhandattack.network.packet;

import com.example.offhandattack.ModTags;
import com.example.offhandattack.capability.OffHandCapabilityProvider;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class OffHandAttackPacket {
    private final int entityId;

    public OffHandAttackPacket(int entityId) {
        this.entityId = entityId;
    }

    public OffHandAttackPacket(FriendlyByteBuf buf) {
        this.entityId = buf.readInt();
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeInt(entityId);
    }

    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player != null) {
                Entity target = player.level().getEntity(entityId);
                if (target != null) {
                    player.getCapability(OffHandCapabilityProvider.OFF_HAND_DATA).ifPresent(data -> {
                        ItemStack mainHand = player.getMainHandItem();
                        ItemStack offHand = player.getOffhandItem();

                        // Verification
                        if (mainHand.getItem() == offHand.getItem() && mainHand.is(ModTags.IS_DUAL)) {
                            // Execute Offhand Attack Logic (based on OffHandCombat)
                            data.setExecutingOffhandAttack(true);
                            
                            int ticksSinceLastSwingOff = data.getOffhandAttackStrengthTicker();
                            int ticksSinceLastSwingMain = player.attackStrengthTicker;

                            // Swap
                            setItemStackToSlot(player, EquipmentSlot.MAINHAND, offHand);
                            setItemStackToSlot(player, EquipmentSlot.OFFHAND, mainHand);
                            makeActive(player, offHand, mainHand);

                            // Attack
                            player.attackStrengthTicker = ticksSinceLastSwingOff;
                            player.attack(target);
                            player.attackStrengthTicker = ticksSinceLastSwingMain;

                            // Swap back
                            setItemStackToSlot(player, EquipmentSlot.OFFHAND, offHand);
                            setItemStackToSlot(player, EquipmentSlot.MAINHAND, mainHand);
                            makeInactive(player, offHand, mainHand);
                            
                            data.setOffhandAttackStrengthTicker(0);

                            data.setExecutingOffhandAttack(false);
                        }
                    });
                }
            }
        });
        context.setPacketHandled(true);
    }

    private void setItemStackToSlot(Player playerIn, EquipmentSlot slotIn, ItemStack stack) {
        if (slotIn == EquipmentSlot.MAINHAND) {
            playerIn.getInventory().items.set(playerIn.getInventory().selected, stack);
        } else if (slotIn == EquipmentSlot.OFFHAND) {
            playerIn.getInventory().offhand.set(0, stack);
        }
    }

    private void makeActive(Player playerIn, ItemStack offhand, ItemStack mainHand) {
        playerIn.getAttributes().removeAttributeModifiers(mainHand.getAttributeModifiers(EquipmentSlot.MAINHAND));
        playerIn.getAttributes().removeAttributeModifiers(offhand.getAttributeModifiers(EquipmentSlot.OFFHAND));
        playerIn.getAttributes().addTransientAttributeModifiers(offhand.getAttributeModifiers(EquipmentSlot.MAINHAND));
        playerIn.getAttributes().addTransientAttributeModifiers(mainHand.getAttributeModifiers(EquipmentSlot.OFFHAND));
    }

    private void makeInactive(Player playerIn, ItemStack offhand, ItemStack mainHand) {
        playerIn.getAttributes().removeAttributeModifiers(mainHand.getAttributeModifiers(EquipmentSlot.OFFHAND));
        playerIn.getAttributes().removeAttributeModifiers(offhand.getAttributeModifiers(EquipmentSlot.MAINHAND));
        playerIn.getAttributes().addTransientAttributeModifiers(mainHand.getAttributeModifiers(EquipmentSlot.MAINHAND));
        playerIn.getAttributes().addTransientAttributeModifiers(offhand.getAttributeModifiers(EquipmentSlot.OFFHAND));
    }
}
