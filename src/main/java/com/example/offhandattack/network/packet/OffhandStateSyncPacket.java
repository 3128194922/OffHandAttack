package com.example.offhandattack.network.packet;

import com.example.offhandattack.capability.OffHandCapabilityProvider;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class OffhandStateSyncPacket {
    private final boolean offhandTurn;
    private final int playerId;

    public OffhandStateSyncPacket(boolean offhandTurn, int playerId) {
        this.offhandTurn = offhandTurn;
        this.playerId = playerId;
    }

    public OffhandStateSyncPacket(FriendlyByteBuf buf) {
        this.offhandTurn = buf.readBoolean();
        this.playerId = buf.readInt();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeBoolean(offhandTurn);
        buf.writeInt(playerId);
    }

    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
                net.minecraft.client.multiplayer.ClientLevel level = Minecraft.getInstance().level;
                if (level != null) {
                    Player player = (Player) level.getEntity(playerId);
                    if (player != null) {
                        player.getCapability(OffHandCapabilityProvider.OFF_HAND_DATA).ifPresent(data -> {
                            data.setOffhandTurn(offhandTurn);
                        });
                    }
                }
            });
        });
        context.setPacketHandled(true);
    }
}
