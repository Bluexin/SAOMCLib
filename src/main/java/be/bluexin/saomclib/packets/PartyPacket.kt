package be.bluexin.saomclib.packets

import be.bluexin.saomclib.capabilities.getPartyCapability
import be.bluexin.saomclib.onServer
import be.bluexin.saomclib.party.PlayerInfo
import be.bluexin.saomclib.readString
import be.bluexin.saomclib.sendPacket
import be.bluexin.saomclib.writeString
import io.netty.buffer.ByteBuf
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.entity.player.EntityPlayerMP
import net.minecraft.util.IThreadListener
import net.minecraftforge.fml.common.network.simpleimpl.IMessage
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext
import java.util.*

class PartyPacket() : IMessage {

    private lateinit var type: Type
    private lateinit var target: String

    constructor(type: Type, target: String): this() {
        this.type = type
        this.target = target
    }

    override fun fromBytes(buf: ByteBuf?) {
        if (buf == null) return
        type = Type.values()[buf.readInt()]
        target = buf.readString()
    }

    override fun toBytes(buf: ByteBuf?) {
        if (buf == null) return
        buf.writeInt(type.ordinal)
        buf.writeString(target)
    }

    companion object {

        class Handler : AbstractServerPacketHandler<PartyPacket>() {
            override fun handleServerPacket(player: EntityPlayer, message: PartyPacket, ctx: MessageContext, mainThread: IThreadListener): IMessage? {
                mainThread.addScheduledTask {
                    val cap = player.getPartyCapability()
                    val party = cap.party
                    val invitedTo = cap.invitedTo
                    val target: EntityPlayer? = player.world.getPlayerEntityByUUID(UUID.fromString(message.target))
                    if (party?.leaderInfo?.player == player) when (message.type) {
                        Type.REMOVE -> if (target != null) party.removeMember(target)
                        Type.INVITE -> if (target != null) party.invite(target)
                        Type.LEADER -> {
                            if (target != null) {
                                party.leaderInfo = PlayerInfo(target)
                                player.world.onServer {
                                    party.membersInfo.mapNotNull { it.player as? EntityPlayerMP }.forEach { it.sendPacket(SyncEntityCapabilityPacket(it.getPartyCapability(), it)) }
                                    party.invitedInfo.mapNotNull { it.key.player as? EntityPlayerMP }.forEach { it.sendPacket(SyncEntityCapabilityPacket(it.getPartyCapability(), it)) }
                                }
                            }
                        }
                        Type.JOIN -> if (target != null)  party.addMember(target)
                        Type.REQUEST -> (player as EntityPlayerMP).sendPacket(SyncEntityCapabilityPacket(player.getPartyCapability(), player))
                        Type.CANCEL -> if (target != null) party.cancel(target)
                    }
                    @Suppress("NON_EXHAUSTIVE_WHEN")
                    if (invitedTo?.isInvited(player) == true) when (message.type) {
                        Type.CANCEL -> invitedTo.cancel(player)
                        Type.JOIN -> invitedTo.addMember(player)
                    }
                    if (player == target && message.type == Type.REMOVE) {
                        party?.removeMember(player)
                    }
                }

                return null
            }

        }
    }

    enum class Type {
        REMOVE,
        INVITE,
        LEADER,
        JOIN,
        REQUEST, // TODO: change this from sync to "apply"-type stuff
        CANCEL,;
    }
}