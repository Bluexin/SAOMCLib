package com.tencao.saomclib.capabilities

import net.minecraft.nbt.NBTBase
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.EnumFacing
import net.minecraftforge.common.capabilities.Capability

/**
 * Part of saomclib by Bluexin.
 *
 * A [Capability.IStorage] implementation that doesn't store anything.
 *
 * @author Bluexin
 */
class NoStorage<T> : Capability.IStorage<T> {
    override fun writeNBT(capability: Capability<T>, instance: T, side: EnumFacing?) = NBTTagCompound()

    override fun readNBT(capability: Capability<T>, instance: T, side: EnumFacing?, nbt: NBTBase) = Unit
}
