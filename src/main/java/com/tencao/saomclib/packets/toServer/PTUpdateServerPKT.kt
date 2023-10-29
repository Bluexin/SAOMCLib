package com.tencao.saomclib.packets.toServer

import com.tencao.saomclib.message
import com.tencao.saomclib.packets.IPacket
import com.tencao.saomclib.packets.PacketPipeline
import com.tencao.saomclib.packets.PartyType
import com.tencao.saomclib.packets.Type
import com.tencao.saomclib.party.PartyManager
import com.tencao.saomclib.party.PlayerInfo
import com.tencao.saomclib.party.playerInfo
import net.minecraft.client.Minecraft
import net.minecraft.entity.player.ServerPlayerEntity
import net.minecraft.network.PacketBuffer
import net.minecraftforge.fml.network.NetworkEvent
import java.util.*

class PTUpdateServerPKT() : IPacket {

    lateinit var partyType: PartyType
    lateinit var type: Type
    var target: UUID = UUID.randomUUID()

    constructor(type: Type, partyType: PartyType) : this() {
        this.type = type
        this.partyType = partyType
    }

    constructor(type: Type, partyType: PartyType, target: UUID) : this() {
        this.type = type
        this.partyType = partyType
        this.target = target
    }

    constructor(type: Type, partyType: PartyType, target: PlayerInfo) : this() {
        this.type = type
        this.partyType = partyType
        this.target = target.uuid
    }

    override fun encode(buffer: PacketBuffer) {
        buffer.writeInt(Type.values().indexOf(type))
        buffer.writeInt(PartyType.values().indexOf(partyType))
        buffer.writeUtf(target.toString())
    }

    override fun handle(context: NetworkEvent.Context) {
        val player = context.sender as? ServerPlayerEntity ?: return
        val party = when (partyType) {
            PartyType.MAIN -> PartyManager.getOrCreateParty(PlayerInfo(player))
            PartyType.INVITE -> PartyManager.getInvitedParty(player.playerInfo()) ?: let {
                player.message("You do not have permission to invite")
                return
            }
        }
        when (type) {
            Type.INVITE -> {
                if (party.isLeader(player)) {
                    party.invite(target)
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
                    party.removeMember(target)
                } else player.message("You do not have permission to kick")
            }
            Type.DISBAND -> {
                if (party.isLeader(player)) {
                    party.dissolve()
                } else player.message("You do not have permission to disband the party")
            }
            Type.LEADERCHANGE -> {
                if (party.isLeader(player)) {
                    if (!party.changeLeader(target)) {
                        player.message("New leader is not in current party")
                    }
                } else player.message("You do not have permission to disband the party")
            }
            else -> return
        }
    }

    companion object {
        fun decode(buffer: PacketBuffer): PTUpdateServerPKT {
            return PTUpdateServerPKT(
                Type.values()[buffer.readInt()],
                PartyType.values()[buffer.readInt()],
                UUID.fromString(buffer.readUtf())
            )
        }
    }
}

/**
 * Sends a packet to the server to update the party
 * data server side, this will not succeed if the
 * required permissions aren't met.
 */
fun Type.updateServer(target: PlayerInfo, type: PartyType) = PacketPipeline.sendToServer(PTUpdateServerPKT(this, type, target))
fun Type.updateServer(type: PartyType) = PacketPipeline.sendToServer(PTUpdateServerPKT(this, type, Minecraft.getInstance().player!!.uuid))
