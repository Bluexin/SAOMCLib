package be.bluexin.saomclib

import be.bluexin.saomclib.packets.PacketPipeline
import io.netty.buffer.ByteBuf
import net.minecraft.block.state.IBlockState
import net.minecraft.client.Minecraft
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.entity.player.EntityPlayerMP
import net.minecraft.init.Blocks
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.EnumFacing
import net.minecraft.util.EnumHand
import net.minecraft.util.math.BlockPos
import net.minecraft.util.text.TextComponentTranslation
import net.minecraft.world.World
import net.minecraftforge.common.ForgeHooks
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.common.util.BlockSnapshot
import net.minecraftforge.event.world.BlockEvent
import net.minecraftforge.fml.common.FMLCommonHandler
import net.minecraftforge.fml.common.network.ByteBufUtils
import net.minecraftforge.fml.common.network.simpleimpl.IMessage

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
    this.mcProfiler.startSection(key)
    body()
    this.mcProfiler.endSection()
}

/**
 * Ensures a block is only called on client side.
 *
 * @param body the block of code to execute only if the current side is client
 */
inline infix fun World.onClient(body: () -> Unit) {
    if (this.isRemote) body()
}

/**
 * Ensures a block is only called on server side.
 *
 * @param body the block of code to execute only if the current side is server
 */
inline infix fun World.onServer(body: () -> Unit) {
    if (!this.isRemote) body()
}

/**
 * Write a UTF8 [String] to a [ByteBuf].
 *
 * @param str the string to write to the [ByteBuf]
 */
fun ByteBuf.writeString(str: String) = ByteBufUtils.writeUTF8String(this, str)

/**
 * Read a UTF8 [String] from a [ByteBuf].
 */
fun ByteBuf.readString() = ByteBufUtils.readUTF8String(this)!!

/**
 * Write a [NBTTagCompound] to a [ByteBuf].
 *
 * @param tag the tag to write to the [ByteBuf]
 */
fun ByteBuf.writeTag(tag: NBTTagCompound) = ByteBufUtils.writeTag(this, tag)

/**
 * Read a [NBTTagCompound] from a [ByteBuf].
 */
fun ByteBuf.readTag() = ByteBufUtils.readTag(this)!!

/**
 * Send a translated text message to a [EntityPlayer].
 */
fun EntityPlayer.message(str: String, vararg args: Any) = this.sendMessage(TextComponentTranslation(str, *args))

/**
 * Send a packet to a player.
 */
fun EntityPlayerMP.sendPacket(packet: IMessage) = PacketPipeline.sendTo(packet, this)
fun EntityPlayer.sentPacketToServer(packet: IMessage) = PacketPipeline.sendToServer(packet)

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
    val evt = BlockEvent.PlaceEvent(before, Blocks.AIR.defaultState, this, EnumHand.MAIN_HAND)
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
