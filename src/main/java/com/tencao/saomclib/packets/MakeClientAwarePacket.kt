package com.tencao.saomclib.packets

import com.tencao.saomclib.SAOMCLib
import io.netty.buffer.ByteBuf
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.util.IThreadListener
import net.minecraftforge.fml.common.network.simpleimpl.IMessage
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext

class MakeClientAwarePacket : IMessage {

    override fun fromBytes(buf: ByteBuf) {
    }

    override fun toBytes(buf: ByteBuf) {
    }

    companion object {
        class Handler : AbstractClientPacketHandler<MakeClientAwarePacket>() {
            override fun handleClientPacket(player: EntityPlayer, message: MakeClientAwarePacket, ctx: MessageContext, mainThread: IThreadListener): IMessage? {
                mainThread.addScheduledTask {
                    SAOMCLib.LOGGER.info("Server check received, setting flag.")
                    SAOMCLib.proxy.isServerSideLoaded = true
                }
                return null
            }
        }
    }
}
