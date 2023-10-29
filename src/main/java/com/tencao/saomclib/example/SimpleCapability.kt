package com.tencao.saomclib.example

import com.tencao.saomclib.SAOMCLib
import com.tencao.saomclib.capabilities.AbstractEntityCapability
import com.tencao.saomclib.capabilities.Key
import net.minecraft.nbt.CompoundNBT
import net.minecraft.nbt.INBT
import net.minecraft.util.Direction
import net.minecraft.util.ResourceLocation
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.common.capabilities.CapabilityInject

/**
 * Part of saomclib by Bluexin.
 *
 * @author Bluexin
 */
class SimpleCapability : AbstractEntityCapability() {

    var num: Int = 0
        set(value) {
            SAOMCLib.LOGGER.info("Set num to $value for ${reference.get()} on remote=${reference.get()?.level?.isClientSide}.")
            field = value
        }

    override fun setup(param: Any): SimpleCapability {
        super.setup(param)
        SAOMCLib.LOGGER.info("Set up for ${param.javaClass}")
        return this
    }

    companion object {
        @Suppress("unused")
        @Key
        val KEY = ResourceLocation(SAOMCLib.MODID, "simpleCapability")

        @CapabilityInject(SimpleCapability::class)
        lateinit var CAP_INSTANCE: Capability<SimpleCapability>

        class Storage : Capability.IStorage<SimpleCapability> {
            override fun writeNBT(capability: Capability<SimpleCapability>, instance: SimpleCapability, side: Direction?): INBT {
                val tag = CompoundNBT()
                tag.putInt("num", instance.num)
                SAOMCLib.LOGGER.info("Writing ${instance.num} on remote=${instance.reference.get()?.level?.isClientSide}.")
                return tag
            }

            override fun readNBT(capability: Capability<SimpleCapability>, instance: SimpleCapability, side: Direction?, nbt: INBT?) {
                instance.num = (nbt as CompoundNBT).getInt("num")
                SAOMCLib.LOGGER.info("Reading ${instance.num} on remote=${instance.reference.get()?.level?.isClientSide}.")
            }
        }
    }
}
