package com.example.offhandattack.event;

import com.example.offhandattack.HandPlatform;
import com.example.offhandattack.OffHandAttackMod;
import com.example.offhandattack.ModTags;
import com.example.offhandattack.capability.OffHandCapabilityProvider;
import com.example.offhandattack.capability.OffHandData;
import com.example.offhandattack.network.NetworkHandler;
import com.example.offhandattack.network.packet.OffHandAttackPacket;
import com.example.offhandattack.network.packet.OffhandStateSyncPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingEquipmentChangeEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = OffHandAttackMod.MODID)
public class ModEvents {

    @SubscribeEvent
    @SuppressWarnings("deprecation")
    public static void onAttachCapabilitiesPlayer(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof Player) {
            if (!event.getObject().getCapability(OffHandCapabilityProvider.OFF_HAND_DATA).isPresent()) {
                event.addCapability(new ResourceLocation(OffHandAttackMod.MODID + ":properties"), new OffHandCapabilityProvider());
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerCloned(PlayerEvent.Clone event) {
        if (event.isWasDeath()) {
            event.getOriginal().getCapability(OffHandCapabilityProvider.OFF_HAND_DATA).ifPresent(oldStore -> {
                event.getEntity().getCapability(OffHandCapabilityProvider.OFF_HAND_DATA).ifPresent(newStore -> {
                    newStore.copyFrom(oldStore);
                });
            });
        }
    }

    @SubscribeEvent
    public static void onPlayerJoin(net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            player.getCapability(OffHandCapabilityProvider.OFF_HAND_DATA).ifPresent(data -> {
                NetworkHandler.sendToPlayer(new OffhandStateSyncPacket(data.isOffhandTurn(), player.getId()), player);
            });
        }
    }

    @SubscribeEvent
    public static void onPlayerRespawn(net.minecraftforge.event.entity.player.PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            player.getCapability(OffHandCapabilityProvider.OFF_HAND_DATA).ifPresent(data -> {
                NetworkHandler.sendToPlayer(new OffhandStateSyncPacket(data.isOffhandTurn(), player.getId()), player);
            });
        }
    }

    @SubscribeEvent
    public static void onAttackEntity(AttackEntityEvent event) {
        Player player = event.getEntity();
        if (player.level().isClientSide) return;

        ItemStack main = player.getMainHandItem();
        ItemStack off = player.getOffhandItem();

        // boolean duelReady = !main.isEmpty() && !off.isEmpty()
        //         && main.is(ModTags.IS_DUEL) && off.is(ModTags.IS_DUEL);
        // boolean duelReady = !main.isEmpty() && !off.isEmpty() && main.is(ModTags.IS_DUEL) && off.is(ModTags.IS_DUEL);
        boolean duelReady = com.example.offhandattack.util.OffHandUtils.isDuelItem(main) && com.example.offhandattack.util.OffHandUtils.isDuelItem(off);
        if (!duelReady) return;

        player.getCapability(OffHandCapabilityProvider.OFF_HAND_DATA).ifPresent(data -> {
            // Toggle turn on every attack
            data.setOffhandTurn(!data.isOffhandTurn());
            // Sync to client
            NetworkHandler.sendToPlayer(new OffhandStateSyncPacket(data.isOffhandTurn(), player.getId()), (ServerPlayer) player);
        });
    }

    @SubscribeEvent
    public static void onLeftClickEmpty(net.minecraftforge.event.entity.player.PlayerInteractEvent.LeftClickEmpty event) {
        // Handled by ClickMixin
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            Player player = event.player;
            ItemStack mainHand = player.getMainHandItem();

            player.getCapability(OffHandCapabilityProvider.OFF_HAND_DATA).ifPresent(data -> {
                // Two-Handed Logic
                if (mainHand.is(ModTags.IS_HANDS)) {
                    // Access raw inventory to bypass Mixin which hides offhand item
                    ItemStack rawOffHand = player.getInventory().offhand.get(0);
                    if (!rawOffHand.isEmpty()) {
                        // Only store if we don't already have one, or maybe drop the new one?
                        // For simplicity, if we have a stored item, we try to add it to inventory first before overwriting
                        if (!data.getStoredOffhandItem().isEmpty()) {
                            if (!player.getInventory().add(data.getStoredOffhandItem())) {
                                player.drop(data.getStoredOffhandItem(), true);
                            }
                        }
                        
                        data.setStoredOffhandItem(rawOffHand.copy());
                        player.setItemSlot(EquipmentSlot.OFFHAND, ItemStack.EMPTY);
                    }
                } else {
                    ItemStack stored = data.getStoredOffhandItem();
                    if (!stored.isEmpty()) {
                        // Access raw inventory
                        ItemStack rawOffHand = player.getInventory().offhand.get(0);
                        if (rawOffHand.isEmpty()) {
                            player.setItemSlot(EquipmentSlot.OFFHAND, stored.copy());
                            data.setStoredOffhandItem(ItemStack.EMPTY);
                        } else {
                            // If something is in offhand, try to add stored to inventory or drop
                            if (!player.getInventory().add(stored.copy())) {
                                player.drop(stored.copy(), true);
                            }
                            data.setStoredOffhandItem(ItemStack.EMPTY);
                        }
                    }
                }
            });
        }
    }
}
