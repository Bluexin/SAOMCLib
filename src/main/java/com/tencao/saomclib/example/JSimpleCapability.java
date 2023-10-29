package com.tencao.saomclib.example;

import com.tencao.saomclib.SAOMCLib;
import com.tencao.saomclib.capabilities.AbstractCapability;
import com.tencao.saomclib.capabilities.AbstractEntityCapability;
import com.tencao.saomclib.capabilities.Key;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import org.jetbrains.annotations.NotNull;

/**
 * Part of saomclib by Bluexin.
 *
 * @author Bluexin
 */
public class JSimpleCapability extends AbstractEntityCapability {
    @Key
    public static ResourceLocation KEY = new ResourceLocation(SAOMCLib.MODID, "simpleCapabilityJava");

    @SuppressWarnings("unused")
    @CapabilityInject(JSimpleCapability.class)
    public static Capability<JSimpleCapability> CAP_INSTANCE;

    private int num = 0;

    @NotNull
    @Override
    public AbstractCapability setup(@NotNull Object param) {
        super.setup(param);
        SAOMCLib.INSTANCE.getLOGGER().info("Set up for " + param.getClass());
        return this;
    }

    public static class Storage implements Capability.IStorage<JSimpleCapability> {
        @Override
        public INBT writeNBT(Capability<JSimpleCapability> capability, JSimpleCapability instance, Direction side) {
            CompoundNBT tag = new CompoundNBT();
            tag.putInt("num", instance.num);
            Entity ref = instance.reference.get();
            SAOMCLib.INSTANCE.getLOGGER().info("Writing " + instance.num + " on remote=" + (ref == null ? "null" : ref.level.isClientSide) + '.');
            return tag;
        }

        @Override
        public void readNBT(Capability<JSimpleCapability> capability, JSimpleCapability instance, Direction side, INBT nbt) {
            instance.num = ((CompoundNBT) nbt).getInt("num");
            Entity ref = instance.reference.get();
            SAOMCLib.INSTANCE.getLOGGER().info("Reading " + instance.num + " on remote=" + (ref == null ? "null" : ref.level.isClientSide) + '.');
        }
    }
}
