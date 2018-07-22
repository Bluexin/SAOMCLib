package be.bluexin.saomclib.packets

import be.bluexin.saomclib.SAOMCLib
import cpw.mods.fml.common.network.simpleimpl.IMessage
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler
import cpw.mods.fml.common.network.simpleimpl.MessageContext
import cpw.mods.fml.relauncher.Side
import cpw.mods.fml.relauncher.SideOnly
import net.minecraft.entity.player.EntityPlayer

/**
 * Abstract class for handling packets.
 * Packets are means of communication between the server and the client, and should implement [IMessage].
 *
 * If your packet doesn't go both ways (server -> client AND client -> server), it is recommended to extend :
 *  - [AbstractClientPacketHandler] for server -> client only
 *  - [AbstractServerPacketHandler] for client -> server only
 *
 * One important consideration to take into account is that networking is threaded in Minecraft.
 * What that means is, when performing anything that has to do with the Minecraft world, you
 * should make a call to [IThreadListener.addScheduledTask] (using the mainThread argument in the handling methods).
 *
 * @author Bluexin
 */
abstract class AbstractPacketHandler<T : IMessage> : IMessageHandler<T, IMessage> {

    /**
     * Handle receiving the packet on the Client side.
     * If your message doesn't need an automated reply, you can safely return null.
     *
     * @param player the player entity of this Client
     * @param ctx the context the packet is received in
     * @param mainThread to be used when performing tasks on the world
     */
    @SideOnly(Side.CLIENT)
    abstract fun handleClientPacket(player: EntityPlayer, message: T, ctx: MessageContext): IMessage?

    /**
     * Handle receiving the packet on the Server side.
     * If your message doesn't need an automated reply, you can safely return null.
     *
     * @param player the player entity that sent the packet
     * @param ctx the context the packet is received in
     * @param mainThread to be used when performing tasks on the world
     */
    abstract fun handleServerPacket(player: EntityPlayer, message: T, ctx: MessageContext): IMessage?

    final override fun onMessage(message: T, ctx: MessageContext): IMessage? {
        val player = SAOMCLib.proxy.getPlayerEntity(ctx)
        return if (player != null) {
            if (ctx.side.isClient) handleClientPacket(player, message, ctx)
            else handleServerPacket(player, message, ctx)
        } else {
            SAOMCLib.LOGGER.info("Received packet before player got initialized.")
            Thread({ Thread.sleep(1000); onMessage(message, ctx) }).start()
            null
        }
    }
}
