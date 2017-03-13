package be.bluexin.saomclib.example

import be.bluexin.saomclib.LogHelper
import be.bluexin.saomclib.SAOMCLib
import be.bluexin.saomclib.capabilities.AbstractEntityCapability
import be.bluexin.saomclib.capabilities.Key
import net.minecraft.entity.Entity
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.ResourceLocation

/**
 * Part of saomclib by Bluexin.
 *
 * @author Bluexin
 */
class SimpleCapability : AbstractEntityCapability() {

    var num: Int = 0
        set(value) {
            LogHelper.logInfo("Set num to $value for ${reference.get()} on remote=${reference.get()?.worldObj?.isRemote}.")
            field = value
        }

    override fun setup(param: Entity): SimpleCapability {
        super.setup(param)
        LogHelper.logInfo("Set up for ${param.javaClass}")
        return this
    }

    override fun loadNBTData(compound: NBTTagCompound) {
        num = compound.getInteger("num")
        LogHelper.logInfo("Reading $num on remote=${reference.get()?.worldObj?.isRemote}.")
    }

    override fun saveNBTData(compound: NBTTagCompound) {
        val tag = NBTTagCompound()
        tag.setInteger("num", num)
        LogHelper.logInfo("Writing $num on remote=${reference.get()?.worldObj?.isRemote}.")
        compound.setTag(KEY.toString(), tag)
    }

    companion object {
        @Suppress("unused")
        @Key val KEY = ResourceLocation(SAOMCLib.MODID, "simpleCapability")
    }
}
