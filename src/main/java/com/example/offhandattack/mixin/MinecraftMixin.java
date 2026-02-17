package com.example.offhandattack.mixin;

import com.example.offhandattack.ModTags;
import com.example.offhandattack.capability.OffHandCapabilityProvider;
import com.example.offhandattack.network.NetworkHandler;
import com.example.offhandattack.network.packet.OffHandAttackPacket;
import com.example.offhandattack.network.packet.OffHandSwingPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin {

    @Shadow public LocalPlayer player;
    @Shadow public HitResult hitResult;
    @Shadow protected int missTime;

    @Inject(method = "startAttack", at = @At("HEAD"), cancellable = true)
    private void onStartAttack(CallbackInfoReturnable<Boolean> cir) {
        if (this.missTime > 0) return;
        if (this.player == null) return; // Safety check
        
        ItemStack main = this.player.getMainHandItem();
        ItemStack off = this.player.getOffhandItem();
        
        // Debug: Log item tags
        // System.out.println("Main: " + main + ", Off: " + off);
        
        // Check for IS_DUEL tag on BOTH items (or config)
        if (com.example.offhandattack.util.OffHandUtils.isDuelItem(main) && com.example.offhandattack.util.OffHandUtils.isDuelItem(off)) {
            // Force capability sync check? No, assume synced.
            this.player.getCapability(OffHandCapabilityProvider.OFF_HAND_DATA).ifPresent(data -> {
                // System.out.println("Client Offhand Turn: " + data.isOffhandTurn());
                if (data.isOffhandTurn()) {
                    // Handle Offhand Attack
                    if (this.hitResult != null && this.hitResult.getType() == HitResult.Type.ENTITY) {
                        Entity entity = ((EntityHitResult)this.hitResult).getEntity();
                        NetworkHandler.sendToServer(new OffHandAttackPacket(entity.getId()));
                        this.player.swing(InteractionHand.OFF_HAND);
                        this.player.resetAttackStrengthTicker();
                    } else {
                        // Miss swing
                        NetworkHandler.sendToServer(new OffHandSwingPacket());
                        this.player.swing(InteractionHand.OFF_HAND);
                        this.player.resetAttackStrengthTicker();
                    }
                    
                    data.setOffhandTurn(false);
                    this.missTime = 10; // Reset miss timer (vanilla behavior)
                    cir.setReturnValue(true); // Cancel vanilla logic, return true (success)
                } else {
                    // Mainhand Turn
                    // Let vanilla logic run (it handles mainhand attack)
                    // Just toggle turn
                    data.setOffhandTurn(true);
                    // Don't cancel!
                }
            });
        }
    }
}
