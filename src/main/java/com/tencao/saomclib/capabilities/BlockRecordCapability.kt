package com.tencao.saomclib.capabilities

import com.tencao.saomclib.SAOMCLib
import net.minecraft.nbt.CompoundNBT
import net.minecraft.nbt.INBT
import net.minecraft.nbt.ListNBT
import net.minecraft.util.Direction
import net.minecraft.util.ResourceLocation
import net.minecraft.util.math.BlockPos
import net.minecraft.world.IWorld
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

        override fun writeNBT(capability: Capability<BlockRecordCapability>?, instance: BlockRecordCapability, side: Direction?): INBT {
            val map = CompoundNBT()
            val coords = ListNBT()

            instance.modifiedBlockCache.forEach { pos ->
                val tag = CompoundNBT()
                tag.putInt("x", pos.x)
                tag.putInt("y", pos.y)
                tag.putInt("z", pos.z)
                coords.add(tag)
            }
            map.put("coords", coords)

            return map
        }

        override fun readNBT(capability: Capability<BlockRecordCapability>?, instance: BlockRecordCapability, side: Direction?, nbt: INBT?) {
            if (nbt !is CompoundNBT) return

            val coords = nbt.getList("coords", 10)

            instance.modifiedBlockCache.clear()

            var tag: CompoundNBT
            for (i in 0 until coords.count()) {
                tag = coords.getCompound(i)
                instance.addBlockPos(tag.getInt("x"), tag.getInt("y"), tag.getInt("z"))
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

fun Chunk.getBlockRecords() = this.getCapability(BlockRecordCapability.CAPABILITY, null).resolve().get()

fun IWorld.getBlockRecords(pos: BlockPos) = (this.getChunk(pos) as? Chunk)?.getBlockRecords()
