package com.tencao.saomclib

import com.tencao.saomclib.packets.IPacket
import com.tencao.saomclib.packets.PacketPipeline
import net.minecraft.block.Blocks
import net.minecraft.client.Minecraft
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.ServerPlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.util.Direction
import net.minecraft.util.math.BlockPos
import net.minecraft.util.text.TranslationTextComponent
import net.minecraft.world.IWorld
import net.minecraft.world.World
import net.minecraftforge.common.ForgeHooks
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.common.util.BlockSnapshot
import net.minecraftforge.event.world.BlockEvent
import net.minecraftforge.fml.server.ServerLifecycleHooks

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
inline fun Minecraft.profile(key: String, body: () -> Unit) {
    this.profiler.startSection(key)
    body()
    this.profiler.endSection()
}

/**
 * Ensures a block is only called on client side.
 *
 * @param body the block of code to execute only if the current side is client
 */
inline infix fun IWorld.onClient(body: () -> Unit) {
    if (this.isRemote) body()
}

/**
 * Ensures a block is only called on server side.
 *
 * @param body the block of code to execute only if the current side is server
 */
inline infix fun IWorld.onServer(body: () -> Unit) {
    if (!this.isRemote) body()
}


/**
 * Send a translated text message to a [EntityPlayer].
 */
fun PlayerEntity.message(str: String, vararg args: Any) = this.sendStatusMessage(TranslationTextComponent(str, *args), false)

/**
 * Send a packet to a player.
 */
fun ServerPlayerEntity.sendPacket(packet: IPacket) = PacketPipeline.sendTo(packet, this)

/**
 * Next 4 :
 * From projectE, with edits mostly made by Tencao
 *
 * Extensions to [EntityPlayerMP] to handle more protection systems.
 *
 * @author Bluexin, Tencao
 */
fun ServerPlayerEntity.hasBreakPermission(pos: BlockPos) = this.hasEditPermission(pos)
        && ForgeHooks.onBlockBreakEvent(this.entityWorld, this.interactionManager.gameType, this, pos) != -1

fun ServerPlayerEntity.hasEditPermission(pos: BlockPos) =
        !ServerLifecycleHooks.getCurrentServer().isBlockProtected(this.serverWorld, pos, this)
                && Direction.values().any { this.canPlayerEdit(pos, it, ItemStack.EMPTY) }

fun ServerPlayerEntity.checkedPlaceBlock(pos: BlockPos): Boolean {
    if (!this.hasEditPermission(pos)) return false
    val world = this.entityWorld
    val before = BlockSnapshot.create(world.dimensionKey, world, pos)
    val evt = BlockEvent.EntityPlaceEvent(before, Blocks.AIR.defaultState, this)
    MinecraftForge.EVENT_BUS.post(evt)
    if (evt.isCanceled) {
        world.restoringBlockSnapshots = true
        before.restore(true, false)
        world.restoringBlockSnapshots = false
        return false
    }
    return true
}

fun ServerPlayerEntity.checkedReplaceBlock(pos: BlockPos) = this.hasBreakPermission(pos)
        && this.checkedPlaceBlock(pos)
