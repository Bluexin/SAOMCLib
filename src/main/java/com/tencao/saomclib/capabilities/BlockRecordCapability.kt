package com.tencao.saomclib.capabilities

import com.tencao.saomclib.SAOMCLib
import net.minecraft.nbt.NBTBase
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.nbt.NBTTagList
import net.minecraft.util.EnumFacing
import net.minecraft.util.ResourceLocation
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import net.minecraft.world.chunk.Chunk
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.common.capabilities.CapabilityInject

class BlockRecordCapability : AbstractCapability() {

    lateinit var chunk: Chunk
    private var modifiedBlockCache: HashSet<BlockPos> = hashSetOf()

    override fun setup(param: Any): AbstractCapability {
        this.chunk = param as Chunk
        return this
    }

    fun addBlockPos(pos: BlockPos) {
        modifiedBlockCache.add(pos)
    }

    fun addBlockPos(x: Int, y: Int, z: Int) {
        addBlockPos(BlockPos(x, y, z))
    }

    /**
     * Will quickly scan the chunk to see if the block has been modified
     */
    fun isBlockModified(pos: BlockPos): Boolean {
        return modifiedBlockCache.contains(pos)
    }

    class Storage : Capability.IStorage<BlockRecordCapability> {
        override fun writeNBT(capability: Capability<BlockRecordCapability>?, instance: BlockRecordCapability, side: EnumFacing?): NBTBase {
            val map = NBTTagCompound()
            val coords = NBTTagList()

            instance.modifiedBlockCache.forEach { pos ->
                val tag = NBTTagCompound()
                tag.setInteger("x", pos.x)
                tag.setInteger("y", pos.y)
                tag.setInteger("z", pos.z)
                coords.appendTag(tag)
            }
            map.setTag("coords", coords)

            return map
        }

        override fun readNBT(capability: Capability<BlockRecordCapability>?, instance: BlockRecordCapability, side: EnumFacing?, nbt: NBTBase?) {
            if (nbt !is NBTTagCompound) return

            val coords = nbt.getTagList("coords", 10)

            instance.modifiedBlockCache.clear()

            var tag: NBTTagCompound
            for (i in 0 until coords.tagCount()) {
                tag = coords.getCompoundTagAt(i)
                instance.addBlockPos(tag.getInteger("x"), tag.getInteger("y"), tag.getInteger("z"))
            }
        }
    }

    companion object {
        @Key
        var KEY = ResourceLocation(SAOMCLib.MODID, "block_data")

        @CapabilityInject(BlockRecordCapability::class)
        lateinit var CAPABILITY: Capability<BlockRecordCapability>
    }
}

fun Chunk.getBlockRecords() = this.getCapability(BlockRecordCapability.CAPABILITY, null)!!

fun World.getBlockRecords(pos: BlockPos) = this.getChunkFromBlockCoords(pos).getBlockRecords()
