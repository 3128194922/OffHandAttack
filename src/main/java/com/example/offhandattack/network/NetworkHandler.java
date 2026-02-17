package com.example.offhandattack.network;

import com.example.offhandattack.OffHandAttackMod;
import com.example.offhandattack.network.packet.OffHandAttackPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
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
                .encoder(OffHandAttackPacket::encode)
                .consumerMainThread(OffHandAttackPacket::handle)
                .add();
    }

    public static <MSG> void sendToServer(MSG message) {
        INSTANCE.sendToServer(message);
    }
}
