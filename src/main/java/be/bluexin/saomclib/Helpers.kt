package be.bluexin.saomclib

import net.minecraft.block.state.IBlockState
import net.minecraft.client.Minecraft
import net.minecraft.entity.player.EntityPlayerMP
import net.minecraft.init.Blocks
import net.minecraft.util.EnumFacing
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import net.minecraftforge.common.ForgeHooks
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.common.util.BlockSnapshot
import net.minecraftforge.event.world.BlockEvent
import net.minecraftforge.fml.common.FMLCommonHandler

/**
 * Part of saomclib by Bluexin.
 *
 * General helper methods/functions.
 * I am not sure how well inlining works in java, but using these in kotlin shouldn't have any performance impact.
 *
 * @author Bluexin
 */

/**
 * Calls a profiled block, with given key.
 */
inline fun profile(mc: Minecraft, key: String, body: () -> Unit) {
    mc.mcProfiler.startSection(key)
    body.invoke()
    mc.mcProfiler.endSection()
}

/**
 * Ensures a block is only called on client side.
 */
inline infix fun World.onClient(body: () -> Unit) {
    if (this.isRemote) body.invoke()
}

/**
 * Ensures a block is only called on server side.
 */
inline infix fun World.onServer(body: () -> Unit) {
    if (!this.isRemote) body.invoke()
}

/**
 * Next 4 :
 * From projectE, with edits mostly made by Tencao
 *
 * Extensions to [EntityPlayerMP] to handle more protection systems.
 *
 * @author Bluexin, Tencao
 */
fun EntityPlayerMP.hasBreakPermission(pos: BlockPos) = this.hasEditPermission(pos)
        && ForgeHooks.onBlockBreakEvent(this.entityWorld, this.interactionManager.gameType, this, pos) != -1

fun EntityPlayerMP.hasEditPermission(pos: BlockPos) =
        !FMLCommonHandler.instance().minecraftServerInstance.isBlockProtected(this.entityWorld, pos, this)
                && EnumFacing.VALUES.any { this.canPlayerEdit(pos, it, null) }

fun EntityPlayerMP.checkedPlaceBlock(pos: BlockPos, state: IBlockState): Boolean {
    if (!this.hasEditPermission(pos)) return false
    val world = this.entityWorld
    val before = BlockSnapshot.getBlockSnapshot(world, pos)
    world.setBlockState(pos, state)
    val evt = BlockEvent.PlaceEvent(before, Blocks.AIR.defaultState, this, null)
    MinecraftForge.EVENT_BUS.post(evt)
    if (evt.isCanceled) {
        world.restoringBlockSnapshots = true
        before.restore(true, false)
        world.restoringBlockSnapshots = false
        return false
    }
    return true
}

fun EntityPlayerMP.checkedReplaceBlock(pos: BlockPos, state: IBlockState) = this.hasBreakPermission(pos)
        && this.checkedPlaceBlock(pos, state)
