package com.example.offhandattack.network.packet;

import com.example.offhandattack.HandPlatform;
import com.example.offhandattack.ModTags;
import com.example.offhandattack.capability.OffHandCapabilityProvider;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
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

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeInt(entityId);
    }

    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null) return;

            Entity target = player.level().getEntity(entityId);
            if (target == null) return;

            // Check distance
            if (player.distanceToSqr(target) > 36.0D) return;

            ItemStack mainHand = player.getMainHandItem();
            ItemStack offHand = player.getOffhandItem();

            // Only allow if both items are dual wield compatible (or at least offhand is)
            if (com.example.offhandattack.util.OffHandUtils.isDuelItem(mainHand) && com.example.offhandattack.util.OffHandUtils.isDuelItem(offHand)) {
                // Swap items to simulate offhand attack with mainhand logic
                HandPlatform.setItemStackToSlot(player, EquipmentSlot.MAINHAND, offHand);
                HandPlatform.setItemStackToSlot(player, EquipmentSlot.OFFHAND, mainHand);

                // Manually update attributes to ensure attack damage is calculated correctly
                // Ensure mainHand attributes are removed (in case they stuck)
                player.getAttributes().removeAttributeModifiers(mainHand.getAttributeModifiers(EquipmentSlot.MAINHAND));
                // Ensure offHand attributes are removed first (to avoid duplicate UUID crash if setItemSlot added them)
                player.getAttributes().removeAttributeModifiers(offHand.getAttributeModifiers(EquipmentSlot.MAINHAND));
                // Add offHand attributes (so attack uses them)
                player.getAttributes().addTransientAttributeModifiers(offHand.getAttributeModifiers(EquipmentSlot.MAINHAND));
                
                // Perform attack
                player.attack(target);
                player.swing(net.minecraft.world.InteractionHand.OFF_HAND, true);

                // Swap back
                HandPlatform.setItemStackToSlot(player, EquipmentSlot.MAINHAND, mainHand);
                HandPlatform.setItemStackToSlot(player, EquipmentSlot.OFFHAND, offHand);
                
                // Revert attributes
                // Remove offHand attributes
                player.getAttributes().removeAttributeModifiers(offHand.getAttributeModifiers(EquipmentSlot.MAINHAND));
                // Ensure mainHand attributes are removed (to avoid duplicate UUID crash)
                player.getAttributes().removeAttributeModifiers(mainHand.getAttributeModifiers(EquipmentSlot.MAINHAND));
                // Add back mainHand attributes
                player.getAttributes().addTransientAttributeModifiers(mainHand.getAttributeModifiers(EquipmentSlot.MAINHAND));
                
                // Update turn and sync
                player.getCapability(OffHandCapabilityProvider.OFF_HAND_DATA).ifPresent(data -> {
                    data.setOffhandTurn(false);
                    // Sync to client to ensure consistency
                    com.example.offhandattack.network.NetworkHandler.sendToPlayer(
                        new com.example.offhandattack.network.packet.OffhandStateSyncPacket(false, player.getId()), player);
                });
            }
        });
        context.setPacketHandled(true);
    }
}
