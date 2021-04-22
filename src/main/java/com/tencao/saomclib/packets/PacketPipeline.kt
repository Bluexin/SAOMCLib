package com.tencao.saomclib.packets

import com.tencao.saomclib.SAOMCLib
import com.tencao.saomclib.packets.PacketPipeline.registerMessage
import com.tencao.saomclib.packets.PacketPipeline.sendTo
import com.tencao.saomclib.sendPacket
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.ServerPlayerEntity
import net.minecraft.network.PacketBuffer
import net.minecraftforge.fml.network.NetworkDirection
import net.minecraftforge.fml.network.NetworkRegistry
import org.apache.http.params.CoreProtocolPNames.PROTOCOL_VERSION
import java.util.*
import java.util.function.Function
import kotlin.reflect.KFunction1
import kotlin.reflect.KFunction2


/**
 * Packet Pipeline to be used.
 * Basic how-to: during [net.minecraftforge.fml.common.event.FMLPreInitializationEvent], register your custom packet with [registerMessage].
 * Then, whenever you need to, you can use [sendTo]-type methods in this class to send packets to a (or many) player(s), to the server, etc.
 *
 * @author Bluexin
 */
object PacketPipeline {

    /**
     * Sends a packet to a specific player (from the server!)
     * @see net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper.sendTo
     * @see [sendPacket] helper method/shortcut
     */
    fun sendTo(message: IPacket, player: ServerPlayerEntity) {
        if (player.connection != null) ntw.sendTo(message, player.connection.netManager, NetworkDirection.PLAY_TO_CLIENT)
    }

    /**
     * Sends a packet to the server (from the client!)
     * @see net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper.sendToServer
     */
    fun sendToServer(message: IPacket) = ntw.sendToServer(message)

    private val ntw = NetworkRegistry.ChannelBuilder
        .named(SAOMCLib.rl("main_channel"))
        .clientAcceptedVersions { anObject: String? ->
            PROTOCOL_VERSION == anObject
        }
        .serverAcceptedVersions { anObject: String? ->
            PROTOCOL_VERSION == anObject
        }
        .networkProtocolVersion { PROTOCOL_VERSION }
        .simpleChannel()

    private var packetId = 0

    /**
     * Registers a packet from the client to the server.
     * Note that all packets require a zero-parameter constructor in order to work.
     *
     * @param type class for the packet itself
     * @param decoder class for handling the packet
     */
    fun <P : IPacket>registerClientToServerMessage(type: Class<P>, decoder: Function<PacketBuffer, P>){
        registerMessage(type, decoder, NetworkDirection.PLAY_TO_SERVER)
    }

    /**
     * Registers a packet from the server to the client.
     * Note that all packets require a zero-parameter constructor in order to work.
     *
     * @param type class for the packet itself
     * @param decoder class for handling the packet
     */
    fun <P : IPacket>registerServerToClientMessage(type: Class<P>, decoder: Function<PacketBuffer, P>){
        registerMessage(type, decoder, NetworkDirection.PLAY_TO_CLIENT)
    }

    fun <P : IPacket>registerMessage(type: Class<P>, decoder: Function<PacketBuffer, P>, networkDirection: NetworkDirection){
        ntw.registerMessage(packetId++, type, IPacket::encode, decoder, IPacket::handle, Optional.of(networkDirection))
    }

    fun <P : IPacket> registerClientToServerMessagee(Class: Class<P>, apply: Function<PacketBuffer, P>) {

    }

}
