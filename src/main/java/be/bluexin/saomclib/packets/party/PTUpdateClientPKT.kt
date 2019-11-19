package be.bluexin.saomclib.packets.party

import be.bluexin.saomclib.capabilities.getPartyCapability
import be.bluexin.saomclib.events.*
import be.bluexin.saomclib.packets.AbstractClientPacketHandler
import be.bluexin.saomclib.party.IPartyData
import be.bluexin.saomclib.party.PartyClientObject
import be.bluexin.saomclib.party.PlayerInfo
import be.bluexin.saomclib.readString
import be.bluexin.saomclib.sendPacket
import be.bluexin.saomclib.writeString
import io.netty.buffer.ByteBuf
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.entity.player.EntityPlayerMP
import net.minecraft.util.IThreadListener
import net.minecraftforge.fml.common.network.ByteBufUtils
import net.minecraftforge.fml.common.network.simpleimpl.IMessage
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext
import java.util.*

class PTUpdateClientPKT(): IMessage {

    lateinit var partyType: PartyType
    lateinit var type: Type
    lateinit var data: IPartyData
    var target: String = ""

    constructor(type: Type, partyType: PartyType, data: IPartyData): this(){
        this.type = type
        this.partyType = partyType
        this.data = data
    }

    constructor(type: Type, partyType: PartyType, data: IPartyData, target: UUID): this(){
        this.type = type
        this.partyType = partyType
        this.data = data
        this.target = target.toString()
    }

    override fun fromBytes(buf: ByteBuf) {
        partyType = PartyType.values()[buf.readInt()]
        type = Type.values()[buf.readInt()]
        data = PartyClientObject.readNBT(ByteBufUtils.readTag(buf))!!
        target = buf.readString()
    }

    override fun toBytes(buf: ByteBuf) {
        buf.writeInt(PartyType.values().indexOf(partyType))
        buf.writeInt(Type.values().indexOf(type))
        ByteBufUtils.writeTag(buf, data.writeNBT())
        buf.writeString(target)
    }


    companion object {
        class Handler : AbstractClientPacketHandler<PTUpdateClientPKT>() {
            override fun handleClientPacket(player: EntityPlayer, message: PTUpdateClientPKT, ctx: MessageContext, mainThread: IThreadListener): IMessage? {
                val partyCap = player.getPartyCapability()
                val target: UUID= if (message.target.isNotEmpty()) try {
                    UUID.fromString(message.target)
                } catch (e: Exception){
                    UUID.randomUUID()
                } else UUID.randomUUID()
                when (message.type){
                    Type.JOIN -> {
                        if (target == player.uniqueID) {
                            partyCap.inviteData = null
                            partyCap.partyData = message.data
                        }
                        else
                            partyCap.setPartyData(message.data, message.partyType)
                        message.data.fireJoin(PlayerInfo(target))
                    }
                    Type.INVITE -> {
                        partyCap.setPartyData(message.data, message.partyType)
                        message.data.fireInvited(PlayerInfo(target))
                    }
                    Type.CANCELINVITE -> {
                        if (target == player.uniqueID)
                            partyCap.inviteData = null
                        else
                            partyCap.setPartyData(message.data, message.partyType)
                        message.data.fireInviteCanceled(PlayerInfo(target))
                    }
                    Type.LEAVE -> {
                        if (target == player.uniqueID)
                            partyCap.partyData = null
                        else
                            partyCap.setPartyData(message.data, message.partyType)
                        message.data.fireInviteCanceled(PlayerInfo(target))
                    }
                    Type.KICK -> {
                        if (target == player.uniqueID)
                            partyCap.partyData = null
                        else
                            partyCap.setPartyData(message.data, message.partyType)
                        message.data.fireKicked(PlayerInfo(target))
                    }
                    Type.DISBAND -> {
                        partyCap.setPartyData(null, message.partyType)
                        message.data.fireDisbanded()
                    }
                    Type.LEADERCHANGE -> {
                        partyCap.setPartyData(message.data, message.partyType)
                        message.data.fireLeaderChanged(PlayerInfo(target))
                    }
                    else -> return null
                }
                return null
            }

        }
    }
}

fun Type.updateClient(player: EntityPlayerMP, data: IPartyData, target: UUID?)
        = player.sendPacket(PTUpdateClientPKT(this, if (player in data) PartyType.MAIN else PartyType.INVITE, data, target ?: player.uniqueID))