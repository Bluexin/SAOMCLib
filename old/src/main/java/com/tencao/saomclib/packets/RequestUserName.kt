package com.tencao.saomclib.packets

import com.tencao.saomclib.readString
import com.tencao.saomclib.writeString
import io.netty.buffer.ByteBuf
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.util.IThreadListener
import net.minecraftforge.fml.common.FMLCommonHandler
import net.minecraftforge.fml.common.network.simpleimpl.IMessage
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext
import java.util.*

// UNUSED
class RequestUserName() : IMessage {

    var data: String = ""

    constructor(uuid: UUID) : this() {
        this.data = uuid.toString()
    }

    constructor(name: String) : this() {
        this.data = name
    }

    override fun fromBytes(buf: ByteBuf) {
        data = buf.readString()
    }

    override fun toBytes(buf: ByteBuf) {
        buf.writeString(data)
    }

    companion object {
        class Handler : AbstractPacketHandler<RequestUserName>() {
            override fun handleClientPacket(player: EntityPlayer, message: RequestUserName, ctx: MessageContext, mainThread: IThreadListener): IMessage? {
                TODO("not implemented") // To change body of created functions use File | Settings | File Templates.
            }

            override fun handleServerPacket(player: EntityPlayer, message: RequestUserName, ctx: MessageContext, mainThread: IThreadListener): IMessage? {
                try {
                    val uuid: UUID = UUID.fromString(message.data)
                    val data = FMLCommonHandler.instance().minecraftServerInstance.playerProfileCache.getProfileByUUID(uuid)
                        ?: return null
                    return RequestUserName(data.name)
                } catch (e: IllegalArgumentException) {
                    return null
                }
            }
        }
    }
}
