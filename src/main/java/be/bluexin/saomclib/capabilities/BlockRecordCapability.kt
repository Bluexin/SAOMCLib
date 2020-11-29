package be.bluexin.saomclib.capabilities

import be.bluexin.saomclib.SAOMCLib
import net.minecraft.nbt.NBTBase
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.nbt.NBTTagList
import net.minecraft.util.EnumFacing
import net.minecraft.util.ResourceLocation
import net.minecraft.util.math.BlockPos
import net.minecraft.world.chunk.Chunk
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.common.capabilities.CapabilityInject

class BlockRecordCapability : AbstractCapability() {

    lateinit var chunk: Chunk
    private var modifiedBlockCache: HashMap<Int, ArrayList<Pair<Int, Int>>> = hashMapOf()

    override fun setup(param: Any): AbstractCapability {
        this.chunk = param as Chunk
        return this
    }

    fun addBlockPos(pos: BlockPos) {
        addBlockPos(pos.x, pos.y, pos.z)
    }

    fun addBlockPos(x: Int, y: Int, z: Int) {
        if (modifiedBlockCache[y]?.add(Pair(x, z)) != true) {
            modifiedBlockCache[y] = arrayListOf(Pair(x, z))
        }
    }

    /**
     * Will quickly scan the chunk to see if the block has been modified
     */
    fun isBlockModified(pos: BlockPos): Boolean {
        return modifiedBlockCache[pos.y]?.any { it.first == pos.x && it.second == pos.z } == true
    }

    class Storage : Capability.IStorage<BlockRecordCapability> {
        override fun writeNBT(capability: Capability<BlockRecordCapability>?, instance: BlockRecordCapability, side: EnumFacing?): NBTBase? {
            val map = NBTTagCompound()
            val coords = NBTTagList()

            instance.modifiedBlockCache.forEach { y, xz ->
                xz.forEach {
                    val tag = NBTTagCompound()
                    tag.setInteger("x", it.first)
                    tag.setInteger("y", y)
                    tag.setInteger("z", it.second)
                    coords.appendTag(tag)
                }
            }
            map.setTag("coords", coords)

            return map
        }

        override fun readNBT(capability: Capability<BlockRecordCapability>?, instance: BlockRecordCapability, side: EnumFacing?, nbt: NBTBase?) {
            if (nbt !is NBTTagCompound) return

            val coords = nbt.getTagList("coords", 10)

            instance.modifiedBlockCache.clear()

            var tag: NBTTagCompound
            for (i in 0 until coords.tagCount()){
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