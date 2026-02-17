package com.example.offhandattack.network.packet;

import com.example.offhandattack.ModTags;
import com.example.offhandattack.capability.OffHandCapabilityProvider;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class OffHandSwingPacket {
    public OffHandSwingPacket() {
    }

    public OffHandSwingPacket(FriendlyByteBuf buf) {
    }

    public void toBytes(FriendlyByteBuf buf) {
    }

    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null) return;

            // Only allow if player has dual wield capability
            player.getCapability(OffHandCapabilityProvider.OFF_HAND_DATA).ifPresent(data -> {
                ItemStack mainHand = player.getMainHandItem();
                ItemStack offHand = player.getOffhandItem();
                
                if (com.example.offhandattack.util.OffHandUtils.isDuelItem(mainHand) && com.example.offhandattack.util.OffHandUtils.isDuelItem(offHand)) {
                    // Just swing the offhand arm
                    player.swing(InteractionHand.OFF_HAND, true);
                    // Update turn to Mainhand and sync
                    data.setOffhandTurn(false);
                    com.example.offhandattack.network.NetworkHandler.sendToPlayer(
                        new com.example.offhandattack.network.packet.OffhandStateSyncPacket(false, player.getId()), player);
                }
            });
        });
        context.setPacketHandled(true);
    }
}
