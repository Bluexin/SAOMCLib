package be.bluexin.saomclib.packets

import be.bluexin.saomclib.events.*
import be.bluexin.saomclib.party.IParty
import be.bluexin.saomclib.party.IPlayerInfo
import be.bluexin.saomclib.party.Party
import be.bluexin.saomclib.party.PlayerInfo
import be.bluexin.saomclib.readString
import be.bluexin.saomclib.writeString
import io.netty.buffer.ByteBuf
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.util.IThreadListener
import net.minecraftforge.fml.common.network.simpleimpl.IMessage
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext
import java.util.*

class ClientPartyPacket(): IMessage {

    lateinit var type: Type
    lateinit var party: IParty
    var target: IPlayerInfo? = null

    constructor(type: Type, party: IParty): this() {
        this.type = type
        this.party = party
    }

    constructor(type: Type, party: IParty, target: IPlayerInfo): this() {
        this.type = type
        this.party = party
        this.target = target
    }

    override fun fromBytes(buf: ByteBuf?) {
        if (buf == null) return
        type = Type.values()[buf.readInt()]
        if (buf.readBoolean()) {
            party = Party(PlayerInfo(UUID.fromString(buf.readString())))
            for (i in 0 until buf.readInt()) {
                party.addMember(PlayerInfo(UUID.fromString(buf.readString())))
            }
        }
        if (buf.readBoolean()){
            target = PlayerInfo(UUID.fromString(buf.readString()))
        }
    }

    override fun toBytes(buf: ByteBuf?) {
        if (buf == null) return
        buf.writeInt(type.ordinal)
        buf.writeString(party.leaderInfo!!.uuidString)
        buf.writeInt(party.membersInfo.count())
        party.membersInfo.forEach { buf.writeString(it.uuidString) }

        buf.writeBoolean(target != null)
        if (target != null) {
            buf.writeString(target!!.uuidString)
        }
    }

    companion object {
        class Handler : AbstractClientPacketHandler<ClientPartyPacket>() {
            override fun handleClientPacket(player: EntityPlayer, message: ClientPartyPacket, ctx: MessageContext, mainThread: IThreadListener): IMessage? {
                mainThread.addScheduledTask {
                    when (message.type){
                        Type.JOIN -> message.party.fireJoin(message.target!!)
                        Type.LEAVE -> message.party.fireLeave(message.target!!)
                        Type.DISBANDED -> message.party.fireDisbanded()
                        Type.KICKED -> message.party.fireKicked(message.target!!)
                        Type.LEADER_CHANGED -> message.party.fireLeaderChanged(message.target!!)
                        Type.INVITED -> message.party.fireInvited(message.target!!)
                        Type.INVITE_CANCELED -> message.party.fireInviteCanceled(message.target!!)
                        Type.REFRESHED -> message.party.fireRefreshed()
                    }
                }
                return null
            }

        }
    }

    enum class Type {
        JOIN,
        LEAVE,
        DISBANDED,
        KICKED,
        LEADER_CHANGED,
        INVITED,
        INVITE_CANCELED,
        REFRESHED;
    }
}