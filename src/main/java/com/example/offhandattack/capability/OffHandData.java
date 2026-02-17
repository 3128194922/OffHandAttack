package com.example.offhandattack.capability;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.INBTSerializable;

public class OffHandData implements INBTSerializable<CompoundTag> {
    private boolean isOffhandTurn = false;
    private ItemStack storedOffhandItem = ItemStack.EMPTY;
    private int offhandAttackStrengthTicker = 0;
    private boolean isExecutingOffhandAttack = false;

    public boolean isOffhandTurn() {
        return isOffhandTurn;
    }

    public void setOffhandTurn(boolean offhandTurn) {
        isOffhandTurn = offhandTurn;
    }

    public ItemStack getStoredOffhandItem() {
        return storedOffhandItem;
    }

    public void setStoredOffhandItem(ItemStack storedOffhandItem) {
        this.storedOffhandItem = storedOffhandItem;
    }
    
    public int getOffhandAttackStrengthTicker() {
        return offhandAttackStrengthTicker;
    }
    
    public void setOffhandAttackStrengthTicker(int ticker) {
        this.offhandAttackStrengthTicker = ticker;
    }
    
    public boolean isExecutingOffhandAttack() {
        return isExecutingOffhandAttack;
    }
    
    public void setExecutingOffhandAttack(boolean executing) {
        this.isExecutingOffhandAttack = executing;
    }

    public void copyFrom(OffHandData source) {
        this.isOffhandTurn = source.isOffhandTurn;
        this.storedOffhandItem = source.storedOffhandItem.copy();
        this.offhandAttackStrengthTicker = source.offhandAttackStrengthTicker;
        this.isExecutingOffhandAttack = source.isExecutingOffhandAttack;
    }

    public void saveNBTData(CompoundTag nbt) {
        nbt.putBoolean("isOffhandTurn", isOffhandTurn);
        nbt.put("storedOffhandItem", storedOffhandItem.save(new CompoundTag()));
        nbt.putInt("offhandAttackStrengthTicker", offhandAttackStrengthTicker);
    }

    public void loadNBTData(CompoundTag nbt) {
        isOffhandTurn = nbt.getBoolean("isOffhandTurn");
        if (nbt.contains("storedOffhandItem")) {
            storedOffhandItem = ItemStack.of(nbt.getCompound("storedOffhandItem"));
        }
        offhandAttackStrengthTicker = nbt.getInt("offhandAttackStrengthTicker");
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag nbt = new CompoundTag();
        saveNBTData(nbt);
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        loadNBTData(nbt);
    }
}
