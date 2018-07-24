package be.bluexin.saomclib

import cpw.mods.fml.relauncher.ReflectionHelper
import net.minecraft.block.Block
import net.minecraft.client.Minecraft
import net.minecraft.entity.Entity
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.nbt.NBTBase
import net.minecraft.nbt.NBTTagList
import net.minecraft.world.World

data class BlockPos(
        val x: Int,
        val y: Int,
        val z: Int
)

data class IBlockState(
        val block: Block,
        val meta: Int = 0
)

inline val Minecraft.player: EntityPlayer?
    get() = this.thePlayer

inline val Entity.world: World
    get() = this.worldObj

inline val Entity.cachedUniqueIdString: String
    get() = this.uniqueID.toString()

inline val EntityPlayer.displayNameString: String
    get() = this.displayName

inline val EntityPlayer.name: String
    get() = this.commandSenderName

private val field_tagList = ReflectionHelper.findField(NBTTagList::class.java, "field_74747_a", "tagList").also {
    it.isAccessible = true
}

fun NBTTagList.asSequence(): Sequence<NBTBase> {
    @Suppress("UNCHECKED_CAST")
    return (field_tagList.get(this) as List<NBTBase>).asSequence()
}