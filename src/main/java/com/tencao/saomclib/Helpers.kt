package com.tencao.saomclib

import com.tencao.saomclib.packets.IPacket
import com.tencao.saomclib.packets.PacketPipeline
import net.minecraft.block.Blocks
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.ServerPlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.util.Direction
import net.minecraft.util.ResourceLocation
import net.minecraft.util.math.BlockPos
import net.minecraft.util.text.TranslationTextComponent
import net.minecraft.world.IWorld
import net.minecraftforge.common.ForgeHooks
import net.minecraftforge.common.util.BlockSnapshot
import net.minecraftforge.common.util.LazyOptional
import net.minecraftforge.event.world.BlockEvent
import net.minecraftforge.fml.server.ServerLifecycleHooks
import thedarkcolour.kotlinforforge.forge.FORGE_BUS
import java.util.*

/**
 * Part of saomclib by Bluexin.
 *
 * General helper methods/functions.
 * I am not sure how well inlining works in java, but using these in kotlin shouldn't have any performance impact.
 *
 * @author Bluexin
 */

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
fun ServerPlayerEntity.hasBreakPermission(pos: BlockPos) = this.hasEditPermission(pos) &&
    ForgeHooks.onBlockBreakEvent(this.entityWorld, this.interactionManager.gameType, this, pos) != -1

fun ServerPlayerEntity.hasEditPermission(pos: BlockPos) =
    !ServerLifecycleHooks.getCurrentServer().isBlockProtected(this.serverWorld, pos, this) &&
        Direction.values().any { this.canPlayerEdit(pos, it, ItemStack.EMPTY) }

fun ServerPlayerEntity.checkedPlaceBlock(pos: BlockPos): Boolean {
    if (!this.hasEditPermission(pos)) return false
    val world = this.entityWorld
    val before = BlockSnapshot.create(world.dimensionKey, world, pos)
    val evt = BlockEvent.EntityPlaceEvent(before, Blocks.AIR.defaultState, this)
    FORGE_BUS.post(evt)
    if (evt.isCanceled) {
        world.restoringBlockSnapshots = true
        before.restore(true, false)
        world.restoringBlockSnapshots = false
        return false
    }
    return true
}

fun ServerPlayerEntity.checkedReplaceBlock(pos: BlockPos) = this.hasBreakPermission(pos) &&
    this.checkedPlaceBlock(pos)

/**
 * Creates a translation key in the format `type.namespace.path[.suffix]`, e.g. `item.minecraft.iron_ingot`
 */
fun ResourceLocation.translationKey(type: String, suffix: String? = null): String =
    "$type.$namespace.$path${suffix?.let { ".$it" } ?: ""}"

fun<T> Optional<T>.getOrNull(): T? = this.orElse(null)

@Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
fun<T> LazyOptional<T>.getOrNull(): T? = this.orElse(null)
