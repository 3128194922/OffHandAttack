package com.example.offhandattack.network;

import com.example.offhandattack.OffHandAttackMod;
import com.example.offhandattack.network.packet.OffHandAttackPacket;
import com.example.offhandattack.network.packet.OffHandSwingPacket;
import com.example.offhandattack.network.packet.OffhandStateSyncPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

public class NetworkHandler {
    private static SimpleChannel INSTANCE;
    private static int packetId = 0;
    private static int id() {
        return packetId++;
    }

    public static void register() {
        SimpleChannel net = NetworkRegistry.ChannelBuilder
                .named(new ResourceLocation(OffHandAttackMod.MODID, "messages"))
                .networkProtocolVersion(() -> "1.0")
                .clientAcceptedVersions(s -> true)
                .serverAcceptedVersions(s -> true)
                .simpleChannel();

        INSTANCE = net;

        net.messageBuilder(OffHandAttackPacket.class, id(), NetworkDirection.PLAY_TO_SERVER)
                .decoder(OffHandAttackPacket::new)
                .encoder(OffHandAttackPacket::toBytes)
                .consumerMainThread(OffHandAttackPacket::handle)
                .add();

        net.messageBuilder(OffHandSwingPacket.class, id(), NetworkDirection.PLAY_TO_SERVER)
                .decoder(OffHandSwingPacket::new)
                .encoder(OffHandSwingPacket::toBytes)
                .consumerMainThread(OffHandSwingPacket::handle)
                .add();
                
        net.messageBuilder(OffhandStateSyncPacket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(OffhandStateSyncPacket::new)
                .encoder(OffhandStateSyncPacket::toBytes)
                .consumerMainThread(OffhandStateSyncPacket::handle)
                .add();
    }

    public static <MSG> void sendToServer(MSG message) {
        INSTANCE.sendToServer(message);
    }

    public static <MSG> void sendToPlayer(MSG message, ServerPlayer player) {
        INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), message);
    }
}
