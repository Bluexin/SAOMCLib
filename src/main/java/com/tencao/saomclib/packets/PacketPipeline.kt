package com.tencao.saomclib.packets

import com.tencao.saomclib.SAOMCLib
import com.tencao.saomclib.except.NoConstructorException
import com.tencao.saomclib.except.UnknownPacketException
import com.tencao.saomclib.packets.PacketPipeline.registerMessage
import com.tencao.saomclib.packets.PacketPipeline.sendTo
import com.tencao.saomclib.sendPacket
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.entity.player.EntityPlayerMP
import net.minecraftforge.fml.common.network.NetworkRegistry
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint
import net.minecraftforge.fml.common.network.simpleimpl.IMessage
import net.minecraftforge.fml.relauncher.Side

/**
 * Packet Pipeline to be used.
 * Basic how-to: during [net.minecraftforge.fml.common.event.FMLPreInitializationEvent], register your custom packet with [registerMessage].
 * Then, whenever you need to, you can use [sendTo]-type methods in this class to send packets to a (or many) player(s), to the server, etc.
 *
 * @author Bluexin
 */
object PacketPipeline {

    /**
     * Registers a packet.
     * Note that all packets require a zero-parameter constructor in order to work.
     *
     * @param messageClass class for the packet itself
     * @param handlerClass class for handling the packet
     * @see IMessage for more info on packets
     * @see AbstractPacketHandler for more info on packet handlers
     */
    fun <REQ : IMessage> registerMessage(messageClass: Class<REQ>, handlerClass: Class<out AbstractPacketHandler<REQ>>) {
        checkValid(messageClass)
        packetz.add(Pair(messageClass, handlerClass))
    }

    /**
     * Sends a packet to a specific player (from the server!)
     * @see net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper.sendTo
     * @see [sendPacket] helper method/shortcut
     */
    fun sendTo(message: IMessage, player: EntityPlayerMP) {
        if (player.connection != null) ntw.sendTo(message, player)
    }

    /**
     * Sends a packet to all the players (from the server!)
     * @see net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper.sendToAll
     */
    fun sendToAll(message: IMessage) = ntw.sendToAll(message)

    /**
     * Sends a packet to all the players around a certain [TargetPoint] (from the server!)
     * @see net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper.sendToAllAround
     */
    fun sendToAllAround(message: IMessage, point: TargetPoint) = ntw.sendToAllAround(message, point)

    /**
     * Sends a packet to all the players around certain coordinates, within range (from the server!)
     * @see net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper.sendToAllAround
     */
    fun sendToAllAround(message: IMessage, dimension: Int, x: Double, y: Double, z: Double, range: Double) = sendToAllAround(message, TargetPoint(dimension, x, y, z, range))

    /**
     * Sends a packet to all the players around certain player, within (from the server!)
     * @see net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper.sendToAllAround
     */
    fun sendToAllAround(message: IMessage, player: EntityPlayer, range: Double) = sendToAllAround(message, player.world.provider.dimension, player.posX, player.posY, player.posZ, range)

    /**
     * Sends a packet to all the players in a certain dimension (from the server!)
     * @see net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper.sendToDimension
     */
    fun sendToDimension(message: IMessage, dimensionId: Int) = ntw.sendToDimension(message, dimensionId)

    /**
     * Sends a packet to the server (from the client!)
     * @see net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper.sendToServer
     */
    fun sendToServer(message: IMessage) = ntw.sendToServer(message)

    private val packetz = ArrayList<Pair<Class<out IMessage>, Class<out AbstractPacketHandler<out IMessage>>>>()
    private val ntw = NetworkRegistry.INSTANCE.newSimpleChannel(SAOMCLib.MODID + "channel")
    private var packetId = 0

    internal fun init() {
        packetz.forEach {
            @Suppress("UNCHECKED_CAST")
            registerImpl(it.first as Class<IMessage>, it.second as Class<out AbstractPacketHandler<IMessage>>)
        }
    }

    private fun <REQ : IMessage> registerImpl(messageClass: Class<REQ>, handlerClass: Class<out AbstractPacketHandler<REQ>>) {
        if (!AbstractServerPacketHandler::class.java.isAssignableFrom(handlerClass)) ntw.registerMessage(handlerClass, messageClass, packetId++, Side.CLIENT)
        if (!AbstractClientPacketHandler::class.java.isAssignableFrom(handlerClass)) ntw.registerMessage(handlerClass, messageClass, packetId++, Side.SERVER)
    }

    private fun checkValid(clazz: Class<out IMessage>) = try {
        clazz.getConstructor().newInstance() ?: throw NoConstructorException(clazz)
    } catch (e: NoSuchMethodException) {
        throw NoConstructorException(clazz)
    } catch (e: Exception) {
        throw UnknownPacketException(clazz, e)
    }
}
