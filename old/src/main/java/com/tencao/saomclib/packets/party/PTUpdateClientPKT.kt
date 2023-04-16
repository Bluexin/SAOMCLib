package com.tencao.saomclib.packets.party

import com.tencao.saomclib.capabilities.getPartyCapability
import com.tencao.saomclib.events.*
import com.tencao.saomclib.packets.AbstractClientPacketHandler
import com.tencao.saomclib.party.IPartyData
import com.tencao.saomclib.party.PartyClientObject
import com.tencao.saomclib.party.PlayerInfo
import com.tencao.saomclib.sendPacket
import io.netty.buffer.ByteBuf
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.entity.player.EntityPlayerMP
import net.minecraft.util.IThreadListener
import net.minecraftforge.fml.common.network.ByteBufUtils
import net.minecraftforge.fml.common.network.simpleimpl.IMessage
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext

class PTUpdateClientPKT() : IMessage {

    lateinit var partyType: PartyType
    lateinit var type: Type
    lateinit var data: IPartyData
    var target: PlayerInfo = PlayerInfo.EMPTY

    constructor(type: Type, partyType: PartyType, data: IPartyData) : this() {
        this.type = type
        this.partyType = partyType
        this.data = data
    }

    constructor(type: Type, partyType: PartyType, data: IPartyData, target: PlayerInfo) : this() {
        this.type = type
        this.partyType = partyType
        this.data = data
        this.target = target
    }

    override fun fromBytes(buf: ByteBuf) {
        partyType = PartyType.values()[buf.readInt()]
        type = Type.values()[buf.readInt()]
        data = PartyClientObject.readNBT(ByteBufUtils.readTag(buf))!!
        target = PlayerInfo.gson.fromJson(ByteBufUtils.readUTF8String(buf), PlayerInfo::class.java)
    }

    override fun toBytes(buf: ByteBuf) {
        buf.writeInt(PartyType.values().indexOf(partyType))
        buf.writeInt(Type.values().indexOf(type))
        ByteBufUtils.writeTag(buf, data.writeNBT())
        ByteBufUtils.writeUTF8String(buf, PlayerInfo.gson.toJson(target))
    }

    companion object {
        class Handler : AbstractClientPacketHandler<PTUpdateClientPKT>() {
            override fun handleClientPacket(player: EntityPlayer, message: PTUpdateClientPKT, ctx: MessageContext, mainThread: IThreadListener): IMessage? {
                val partyCap = player.getPartyCapability()
                when (message.type) {
                    Type.JOIN -> {
                        if (message.target.equals(player.uniqueID)) {
                            partyCap.inviteData.removeIf { it.isLeader(message.data.leaderInfo) }
                            partyCap.partyData = message.data
                        } else {
                            partyCap.setPartyData(message.data, message.partyType)
                        }
                        message.data.fireJoin(message.target)
                        message.data.fireRefresh()
                    }
                    Type.INVITE -> {
                        partyCap.setPartyData(message.data, message.partyType)
                        message.data.fireInvited(message.target)
                        message.data.fireRefresh()
                    }
                    Type.CANCELINVITE -> {
                        if (message.target.equals(player.uniqueID)) {
                            partyCap.inviteData.removeIf { it.isLeader(message.data.leaderInfo) }
                        } else {
                            partyCap.setPartyData(message.data, message.partyType)
                        }
                        message.data.fireInviteCanceled(message.target)
                        message.data.fireRefresh()
                    }
                    Type.LEAVE -> {
                        if (message.target.equals(player.uniqueID)) {
                            partyCap.partyData = null
                        } else {
                            partyCap.setPartyData(message.data, message.partyType)
                        }
                        message.data.fireInviteCanceled(message.target)
                        message.data.fireRefresh()
                    }
                    Type.KICK -> {
                        if (message.target.equals(player.uniqueID)) {
                            partyCap.partyData = null
                        } else {
                            partyCap.setPartyData(message.data, message.partyType)
                        }
                        message.data.fireKicked(message.target)
                        message.data.fireRefresh()
                    }
                    Type.DISBAND -> {
                        partyCap.setPartyData(null, message.partyType)
                        message.data.fireDisbanded()
                        message.data.fireRefresh()
                    }
                    Type.LEADERCHANGE -> {
                        when (message.partyType) {
                            PartyType.MAIN -> {
                                partyCap.setPartyData(message.data, message.partyType)
                            }
                            PartyType.INVITE -> {
                                partyCap.inviteData.removeIf { it.isLeader(message.target) }
                                partyCap.inviteData.add(message.data)
                            }
                        }
                        message.data.fireLeaderChanged(message.data.leaderInfo, message.target)
                        message.data.fireRefresh()
                    }
                    Type.REFRESH -> {
                        partyCap.setPartyData(message.data, message.partyType)
                        message.data.fireRefresh()
                    }
                    else -> return null
                }
                // Fire refresh last so all other changes can occur
                message.data.fireRefresh()
                return null
            }
        }
    }
}

fun Type.updateClient(player: EntityPlayerMP, data: IPartyData, target: PlayerInfo) =
    player.sendPacket(PTUpdateClientPKT(this, if (player in data) PartyType.MAIN else PartyType.INVITE, data, target))
