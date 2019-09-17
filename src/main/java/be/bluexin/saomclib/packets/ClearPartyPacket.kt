package be.bluexin.saomclib.packets

import be.bluexin.saomclib.capabilities.getPartyCapability
import io.netty.buffer.ByteBuf
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.util.IThreadListener
import net.minecraftforge.fml.common.network.simpleimpl.IMessage
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext

class ClearPartyPacket(): IMessage {

    private lateinit var type: Type

    constructor(type: Type): this() {
        this.type = type
    }

    override fun fromBytes(buf: ByteBuf?) {
        if (buf == null) return
        type = Type.values()[buf.readInt()]
    }

    override fun toBytes(buf: ByteBuf?) {
        if (buf == null) return
        buf.writeInt(type.ordinal)
    }


    companion object {
        class Handler : AbstractClientPacketHandler<ClearPartyPacket>() {
            override fun handleClientPacket(player: EntityPlayer, message: ClearPartyPacket, ctx: MessageContext, mainThread: IThreadListener): IMessage? {
                mainThread.addScheduledTask {
                    when (message.type){
                        Type.PARTY -> player.getPartyCapability().party = null
                        Type.INVITE -> player.getPartyCapability().invitedTo = null
                    }
                }
                return null
            }

        }
    }

    enum class Type {
        PARTY,
        INVITE;
    }
}