package com.tencao.saomclib.packets

import net.minecraft.network.PacketBuffer
import net.minecraftforge.fml.network.NetworkEvent
import java.util.function.Supplier

interface IPacket {

    fun handle(context: NetworkEvent.Context)

    fun encode(buffer: PacketBuffer)

    companion object {

        @JvmStatic fun <P : IPacket> handle(message: P, ctx: Supplier<NetworkEvent.Context>) {
            val context: NetworkEvent.Context = ctx.get()
            context.enqueueWork { message.handle(context) }
            context.packetHandled = true
        }
    }
}
