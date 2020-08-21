package be.bluexin.saomclib.packets

import be.bluexin.saomclib.SAOMCLib
import io.netty.buffer.ByteBuf
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.util.IThreadListener
import net.minecraftforge.fml.common.network.simpleimpl.IMessage
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext

class MakeClientAwarePacket: IMessage {

    var isServerSideLoaded: Boolean = false

    constructor(isServerSideLoaded: Boolean){
        this.isServerSideLoaded = isServerSideLoaded
    }

    override fun fromBytes(buf: ByteBuf) {
        isServerSideLoaded = buf.readBoolean()
    }

    override fun toBytes(buf: ByteBuf) {
        buf.writeBoolean(isServerSideLoaded)
    }


    companion object{
        class Handler: AbstractClientPacketHandler<MakeClientAwarePacket>(){
            override fun handleClientPacket(player: EntityPlayer, message: MakeClientAwarePacket, ctx: MessageContext, mainThread: IThreadListener): IMessage? {
                SAOMCLib.proxy.isServerSideLoaded = message.isServerSideLoaded
                return null
            }
        }
    }
}