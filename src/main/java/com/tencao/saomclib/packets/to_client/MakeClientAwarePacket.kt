package com.tencao.saomclib.packets.to_client

import com.tencao.saomclib.SAOMCLib
import com.tencao.saomclib.packets.IPacket
import net.minecraft.network.PacketBuffer
import net.minecraftforge.fml.network.NetworkEvent

class MakeClientAwarePacket(): IPacket {

    var isServerSideLoaded: Boolean = false

    constructor(isServerSideLoaded: Boolean): this(){
        this.isServerSideLoaded = isServerSideLoaded
    }

    override fun encode(buffer: PacketBuffer) {
        buffer.writeBoolean(isServerSideLoaded)
    }

    override fun handle(context: NetworkEvent.Context) {
        SAOMCLib.proxy.isServerSideLoaded = isServerSideLoaded
    }

    companion object {
        fun decode(buffer: PacketBuffer): MakeClientAwarePacket {
            return MakeClientAwarePacket(buffer.readBoolean())
        }
    }
}