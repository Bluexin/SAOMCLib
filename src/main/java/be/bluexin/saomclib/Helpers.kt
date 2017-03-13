package be.bluexin.saomclib

import be.bluexin.saomclib.packets.PacketPipeline
import cpw.mods.fml.common.network.ByteBufUtils
import cpw.mods.fml.common.network.simpleimpl.IMessage
import io.netty.buffer.ByteBuf
import net.minecraft.client.Minecraft
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.entity.player.EntityPlayerMP
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.ChatComponentTranslation
import net.minecraft.world.World

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
 *
 * @param body the block of code to execute only if the current side is client
 */
inline infix fun World.onClient(body: () -> Unit) {
    if (this.isRemote) body.invoke()
}

/**
 * Ensures a block is only called on server side.
 *
 * @param body the block of code to execute only if the current side is server
 */
inline infix fun World.onServer(body: () -> Unit) {
    if (!this.isRemote) body.invoke()
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
fun EntityPlayer.message(str: String, vararg args: Any) = this.addChatComponentMessage(ChatComponentTranslation(str, *args))

/**
 * Send a packet to a player.
 */
fun EntityPlayerMP.sendPacket(packet: IMessage) = PacketPipeline.sendTo(packet, this)

private class Helpers // Maybe this will help gradle keeping the source in the source jar?
