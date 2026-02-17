package com.example.offhandattack.capability;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.INBTSerializable;

public class OffHandData implements INBTSerializable<CompoundTag> {
    private ItemStack storedOffhandItem = ItemStack.EMPTY;
    private boolean offhandTurn = false;

    public ItemStack getStoredOffhandItem() {
        return storedOffhandItem;
    }

    public void setStoredOffhandItem(ItemStack storedOffhandItem) {
        this.storedOffhandItem = storedOffhandItem;
    }

    public void copyFrom(OffHandData source) {
        this.storedOffhandItem = source.storedOffhandItem.copy();
        this.offhandTurn = source.offhandTurn;
    }

    public void saveNBTData(CompoundTag nbt) {
        nbt.put("storedOffhandItem", storedOffhandItem.save(new CompoundTag()));
        nbt.putBoolean("offhandTurn", offhandTurn);
    }

    public void loadNBTData(CompoundTag nbt) {
        if (nbt.contains("storedOffhandItem")) {
            storedOffhandItem = ItemStack.of(nbt.getCompound("storedOffhandItem"));
        } else {
            storedOffhandItem = ItemStack.EMPTY;
        }
        offhandTurn = nbt.getBoolean("offhandTurn");
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

    public boolean isOffhandTurn() {
        return offhandTurn;
    }

    public void setOffhandTurn(boolean offhandTurn) {
        this.offhandTurn = offhandTurn;
    }
}
