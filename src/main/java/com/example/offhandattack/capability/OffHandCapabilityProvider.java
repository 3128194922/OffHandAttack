package com.example.offhandattack.capability;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class OffHandCapabilityProvider implements ICapabilityProvider, INBTSerializable<CompoundTag> {
    public static Capability<OffHandData> OFF_HAND_DATA = CapabilityManager.get(new CapabilityToken<OffHandData>() {
    });

    private OffHandData offHandData = null;
    private final LazyOptional<OffHandData> optional = LazyOptional.of(this::createOffHandData);

    private OffHandData createOffHandData() {
        if (this.offHandData == null) {
            this.offHandData = new OffHandData();
        }
        return this.offHandData;
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == OFF_HAND_DATA) {
            return optional.cast();
        }
        return LazyOptional.empty();
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag nbt = new CompoundTag();
        createOffHandData().saveNBTData(nbt);
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        createOffHandData().loadNBTData(nbt);
    }
}
