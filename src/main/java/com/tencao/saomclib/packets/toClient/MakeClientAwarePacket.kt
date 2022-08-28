package com.tencao.saomclib.packets.toClient

import com.tencao.saomclib.SAOMCLib
import com.tencao.saomclib.packets.IPacket
import net.minecraft.network.PacketBuffer
import net.minecraftforge.fml.network.NetworkEvent

/**
 * Lets the client aware that SAOMCLib is present on the server
 */
class MakeClientAwarePacket() : IPacket {

    override fun encode(buffer: PacketBuffer) {
    }

    override fun handle(context: NetworkEvent.Context) {
        SAOMCLib.proxy.isServerSideLoaded = true
    }

    companion object {
        fun decode(buffer: PacketBuffer): MakeClientAwarePacket {
            return MakeClientAwarePacket()
        }
    }
}
