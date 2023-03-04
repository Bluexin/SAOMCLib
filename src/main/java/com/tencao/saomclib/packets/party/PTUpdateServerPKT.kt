package com.tencao.saomclib.packets.party

import com.tencao.saomclib.message
import com.tencao.saomclib.packets.AbstractServerPacketHandler
import com.tencao.saomclib.packets.PacketPipeline
import com.tencao.saomclib.party.PartyManager
import com.tencao.saomclib.party.PlayerInfo
import com.tencao.saomclib.party.playerInfo
import com.tencao.saomclib.party.toPlayerInfo
import com.tencao.saomclib.readString
import com.tencao.saomclib.writeString
import io.netty.buffer.ByteBuf
import net.minecraft.client.Minecraft
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.util.IThreadListener
import net.minecraftforge.fml.common.network.simpleimpl.IMessage
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext
import java.util.*

class PTUpdateServerPKT() : IMessage {

    lateinit var partyType: PartyType
    lateinit var type: Type
    var target: UUID = UUID.randomUUID()
    var partyLeader: UUID = UUID( 0, 0)

    constructor(type: Type, partyType: PartyType) : this() {
        this.type = type
        this.partyType = partyType
    }

    constructor(type: Type, partyType: PartyType, target: UUID, leader: UUID = UUID( 0, 0)) : this() {
        this.type = type
        this.partyType = partyType
        this.target = target
        this.partyLeader = UUID.randomUUID()
    }

    constructor(type: Type, partyType: PartyType, target: PlayerInfo, leader: PlayerInfo = PlayerInfo.EMPTY) : this() {
        this.type = type
        this.partyType = partyType
        this.target = target.uuid
        this.partyLeader = leader.uuid
    }

    override fun fromBytes(buf: ByteBuf) {
        partyType = PartyType.values()[buf.readInt()]
        type = Type.values()[buf.readInt()]
        target = UUID.fromString(buf.readString())
        partyLeader = UUID.fromString(buf.readString())
    }

    override fun toBytes(buf: ByteBuf) {
        buf.writeInt(PartyType.values().indexOf(partyType))
        buf.writeInt(Type.values().indexOf(type))
        buf.writeString(target.toString())
        buf.writeString(partyLeader.toString())
    }

    companion object {
        class Handler : AbstractServerPacketHandler<PTUpdateServerPKT>() {
            override fun handleServerPacket(player: EntityPlayer, message: PTUpdateServerPKT, ctx: MessageContext, mainThread: IThreadListener): IMessage? {
                val party = when (message.partyType) {
                    PartyType.MAIN -> PartyManager.getOrCreateParty(PlayerInfo(player))
                    PartyType.INVITE -> PartyManager.getInvitedParty(player.playerInfo(), message.partyLeader.toPlayerInfo()) ?: let {
                        return null
                    }
                }
                when (message.type) {
                    Type.INVITE -> {
                        if (party.isLeader(player)) {
                            party.invite(message.target)
                        } else player.message("You do not have permission to invite")
                    }
                    Type.ACCEPTINVITE -> {
                        party.acceptInvite(player)
                    }
                    Type.CANCELINVITE -> {
                        party.cancel(player)
                    }
                    Type.LEAVE -> {
                        party.removeMember(player)
                    }
                    Type.KICK -> {
                        if (party.isLeader(player)) {
                            party.removeMember(message.target)
                        } else player.message("You do not have permission to kick")
                    }
                    Type.DISBAND -> {
                        if (party.isLeader(player)) {
                            party.dissolve()
                        } else player.message("You do not have permission to disband the party")
                    }
                    Type.LEADERCHANGE -> {
                        if (party.isLeader(player)) {
                            if (!party.changeLeader(message.target)) {
                                player.message("New leader is not in current party")
                            }
                        } else player.message("You do not have permission to disband the party")
                    }
                    else -> return null
                }
                return null
            }
        }
    }
}

/**
 * Sends a packet to the server to update the party
 * data server side, this will not succeed if the
 * required permissions aren't met.
 *
 * @param target - The target of the action, for example the person invited
 * @param leader - The current leader of the party, only used for invited
 * @param type - Is this the main party or invited party
 */

fun Type.updateServer(target: PlayerInfo, leader: PlayerInfo) = PacketPipeline.sendToServer(PTUpdateServerPKT(this, PartyType.INVITE, target, leader))
fun Type.updateServer(target: PlayerInfo, type: PartyType) = PacketPipeline.sendToServer(PTUpdateServerPKT(this, type, target))
fun Type.updateServer(type: PartyType) = PacketPipeline.sendToServer(PTUpdateServerPKT(this, type, Minecraft.getMinecraft().player.uniqueID))
