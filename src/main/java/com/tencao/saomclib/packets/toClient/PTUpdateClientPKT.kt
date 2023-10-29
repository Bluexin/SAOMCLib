package com.tencao.saomclib.packets.toClient

import com.tencao.saomclib.capabilities.getPartyCapability
import com.tencao.saomclib.events.*
import com.tencao.saomclib.packets.IPacket
import com.tencao.saomclib.packets.PartyType
import com.tencao.saomclib.packets.Type
import com.tencao.saomclib.party.IPartyData
import com.tencao.saomclib.party.PartyClientObject
import com.tencao.saomclib.party.PlayerInfo
import com.tencao.saomclib.sendPacket
import net.minecraft.client.Minecraft
import net.minecraft.entity.player.ServerPlayerEntity
import net.minecraft.nbt.CompoundNBT
import net.minecraft.network.PacketBuffer
import net.minecraftforge.fml.network.NetworkEvent

class PTUpdateClientPKT() : IPacket {

    lateinit var partyType: PartyType
    lateinit var type: Type
    var data: IPartyData? = null
    var target: PlayerInfo = PlayerInfo.EMPTY

    constructor(type: Type, partyType: PartyType, data: IPartyData?) : this() {
        this.type = type
        this.partyType = partyType
        this.data = data
    }

    constructor(type: Type, partyType: PartyType, data: IPartyData?, target: PlayerInfo) : this() {
        this.type = type
        this.partyType = partyType
        this.data = data
        this.target = target
    }

    override fun encode(buffer: PacketBuffer) {
        buffer.writeInt(Type.values().indexOf(type))
        buffer.writeInt(PartyType.values().indexOf(partyType))
        buffer.writeNbt(data?.writeNBT() ?: CompoundNBT())
        buffer.writeUtf(PlayerInfo.gson.toJson(target))
    }

    override fun handle(context: NetworkEvent.Context) {
        val player = Minecraft.getInstance().player ?: return
        val partyCap = player.getPartyCapability() ?: return
        val partyData = data ?: return
        when (type) {
            Type.JOIN -> {
                if (target.equals(player.uuid)) {
                    partyCap.inviteData.removeIf { it.isLeader(partyData.leaderInfo) }
                    partyCap.partyData = partyData
                } else {
                    partyCap.setPartyData(partyData, partyType)
                }
                partyData.fireJoin(target)
                partyData.fireRefresh()
            }
            Type.INVITE -> {
                partyCap.setPartyData(partyData, partyType)
                partyData.fireInvited(target)
                partyData.fireRefresh()
            }
            Type.CANCELINVITE -> {
                if (target.equals(player.uuid)) {
                    partyCap.inviteData.removeIf { it.isLeader(partyData.leaderInfo) }
                } else {
                    partyCap.setPartyData(partyData, partyType)
                }
                partyData.fireInviteCanceled(target)
                partyData.fireRefresh()
            }
            Type.LEAVE -> {
                if (target.equals(player.uuid)) {
                    partyCap.partyData = null
                } else {
                    partyCap.setPartyData(partyData, partyType)
                }
                partyData.fireInviteCanceled(target)
                partyData.fireRefresh()
            }
            Type.KICK -> {
                if (target.equals(player.uuid)) {
                    partyCap.partyData = null
                } else {
                    partyCap.setPartyData(partyData, partyType)
                }
                partyData.fireKicked(target)
                partyData.fireRefresh()
            }
            Type.DISBAND -> {
                partyCap.setPartyData(null, partyType)
                partyData.fireDisbanded()
                partyData.fireRefresh()
            }
            Type.LEADERCHANGE -> {
                when (partyType) {
                    PartyType.MAIN -> {
                        partyCap.setPartyData(partyData, partyType)
                    }
                    PartyType.INVITE -> {
                        partyCap.inviteData.removeIf { it.isLeader(target) }
                        partyCap.inviteData.add(partyData)
                    }
                }
                partyData.fireLeaderChanged(partyData.leaderInfo, target)
                partyData.fireRefresh()
            }
            Type.REFRESH -> {
                partyCap.setPartyData(partyData, partyType)
                partyData.fireRefresh()
            }
            else -> return
        }
        // Fire refresh last so all other changes can occur
        partyData.fireRefresh()
    }

    companion object {
        fun decode(buffer: PacketBuffer): PTUpdateClientPKT {
            return PTUpdateClientPKT(
                Type.values()[buffer.readInt()],
                PartyType.values()[buffer.readInt()],
                PartyClientObject.readNBT(buffer.readNbt()),
                PlayerInfo.gson.fromJson(buffer.readUtf(), PlayerInfo::class.java)
            )
        }
    }
}

fun Type.updateClient(player: ServerPlayerEntity, data: IPartyData, target: PlayerInfo) =
    player.sendPacket(PTUpdateClientPKT(this, if (player in data) PartyType.MAIN else PartyType.INVITE, data, target))
