package com.tencao.saomclib.capabilities

import net.minecraft.nbt.CompoundNBT
import net.minecraft.nbt.INBT
import net.minecraft.util.Direction
import net.minecraftforge.common.capabilities.Capability

/**
 * Part of saomclib by Bluexin.
 *
 * A [Capability.IStorage] implementation that doesn't store anything.
 *
 * @author Bluexin
 */
class NoStorage<T> : Capability.IStorage<T> {
    override fun writeNBT(capability: Capability<T>, instance: T, side: Direction?) = CompoundNBT()

    override fun readNBT(capability: Capability<T>, instance: T, side: Direction?, nbt: INBT) = Unit
}
