package com.tencao.saomclib.packets

import net.minecraft.entity.player.EntityPlayer
import net.minecraft.util.IThreadListener
import net.minecraftforge.fml.common.network.simpleimpl.IMessage
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext

/**
 * Abstract class for handling packets that are meant to be received on the Server side.
 * @see AbstractPacketHandler for more info
 *
 * @author Bluexin
 */
abstract class AbstractServerPacketHandler<T : IMessage> : AbstractPacketHandler<T>() {
    final override fun handleClientPacket(player: EntityPlayer, message: T, ctx: MessageContext, mainThread: IThreadListener): IMessage? = null
}
