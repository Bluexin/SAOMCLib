package be.bluexin.saomclib.example

import be.bluexin.saomclib.LogHelper
import be.bluexin.saomclib.SAOMCLib
import be.bluexin.saomclib.capabilities.AbstractEntityCapability
import be.bluexin.saomclib.capabilities.Key
import net.minecraft.nbt.NBTBase
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.EnumFacing
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
            LogHelper.logInfo("Set num to $value for ${reference.get()} on remote=${reference.get()?.world?.isRemote}.")
            field = value
        }

    override fun setup(param: Any): SimpleCapability {
        super.setup(param)
        LogHelper.logInfo("Set up for ${param.javaClass}")
        return this
    }

    companion object {
        @Suppress("unused")
        @Key val KEY = ResourceLocation(SAOMCLib.MODID, "simpleCapability")

        @CapabilityInject(SimpleCapability::class)
        lateinit var CAP_INSTANCE: Capability<SimpleCapability>

        class Storage : Capability.IStorage<SimpleCapability> {
            override fun writeNBT(capability: Capability<SimpleCapability>, instance: SimpleCapability, side: EnumFacing?): NBTBase {
                val tag = NBTTagCompound()
                tag.setInteger("num", instance.num)
                LogHelper.logInfo("Writing ${instance.num} on remote=${instance.reference.get()?.world?.isRemote}.")
                return tag
            }

            override fun readNBT(capability: Capability<SimpleCapability>, instance: SimpleCapability, side: EnumFacing?, nbt: NBTBase?) {
                instance.num = (nbt as NBTTagCompound).getInteger("num")
                LogHelper.logInfo("Reading ${instance.num} on remote=${instance.reference.get()?.world?.isRemote}.")
            }

        }
    }
}
