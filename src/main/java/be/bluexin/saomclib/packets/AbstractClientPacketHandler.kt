package be.bluexin.saomclib.packets

import cpw.mods.fml.common.network.simpleimpl.IMessage
import cpw.mods.fml.common.network.simpleimpl.MessageContext
import net.minecraft.entity.player.EntityPlayer

/**
 * Abstract class for handling packets that are meant to be received on the Client side.
 * @see AbstractPacketHandler for more info
 *
 * @author Bluexin
 */
abstract class AbstractClientPacketHandler<T : IMessage> : AbstractPacketHandler<T>() {
    final override fun handleServerPacket(player: EntityPlayer, message: T, ctx: MessageContext): IMessage? = null
}
