package be.bluexin.saomclib.packets

import be.bluexin.saomclib.LogHelper
import be.bluexin.saomclib.capabilities.getPartyCapability
import be.bluexin.saomclib.party.Party
import be.bluexin.saomclib.readString
import be.bluexin.saomclib.writeString
import cpw.mods.fml.common.network.simpleimpl.IMessage
import cpw.mods.fml.common.network.simpleimpl.MessageContext
import io.netty.buffer.ByteBuf
import net.minecraft.entity.player.EntityPlayer
import java.util.*

/**
 * Part of saomclib by Bluexin.
 *
 * @author Bluexin
 */
class PTPacket() : IMessage {

    private lateinit var type: Type
    private lateinit var leader: String
    private lateinit var members: List<String>

    /**
     * @param type the type of action
     * @param leader the leader of the party, or the member to add (depending on type of packet)
     * @param members the party list (may be empty when not required by [type]
     */
    constructor(type: Type, leader: EntityPlayer, members: List<EntityPlayer>) : this() {
        this.type = type
        this.leader = leader.uniqueID.toString()
        this.members = members.map { it.uniqueID.toString() }
    }

    override fun fromBytes(buf: ByteBuf) {
        type = Type.values()[buf.readInt()]
        leader = buf.readString()
        members = buf.readString().split(" ")
    }

    override fun toBytes(buf: ByteBuf) {
        buf.writeInt(type.ordinal)
        buf.writeString(leader)
        buf.writeString(members.joinToString(separator = " ") { it })
    }

    companion object {
        enum class Type {
            ADD,
            REMOVE,
            CLEAR,
            INVITE,
            LEADER,
            JOIN
        }

        class Handler : AbstractClientPacketHandler<PTPacket>() {
            override fun handleClientPacket(player: EntityPlayer, message: PTPacket, ctx: MessageContext): IMessage? {
                val p1 = player.worldObj.getPlayerEntityByUUID(UUID.fromString(message.leader))
                LogHelper.logInfo("${player.displayName} received ${message.type} with p1 ${p1?.displayName}")
                    try {
                        when (message.type) {
                            Type.ADD -> if (p1 != null) player.getPartyCapability().getOrCreatePT().addMember(p1)
                            Type.REMOVE -> if (p1 != null) player.getPartyCapability().party?.removeMember(p1)
                            Type.CLEAR -> player.getPartyCapability().clear()
                            Type.INVITE -> {
                                if (p1 != null) {
                                    val pt = Party(p1)
                                    message.members.map { player.worldObj.getPlayerEntityByUUID(UUID.fromString(it)) }
                                            .filterNotNull().forEach { pt.addMember(it) }
                                    player.getPartyCapability().invitedTo = pt
                                }
                            }
                            Type.LEADER -> if (p1 != null) player.getPartyCapability().party?.leader = p1
                            Type.JOIN -> {
                                if (p1 != null) {
                                    val pt = Party(p1)
                                    message.members.map { player.worldObj.getPlayerEntityByUUID(UUID.fromString(it)) }
                                            .filterNotNull().forEach { pt.addMember(it) }
                                    pt.addMember(player)
                                    val cap = player.getPartyCapability()
                                    cap.party = pt
                                    cap.invitedTo = null
                                }
                            }
                        }
                    } catch (e: Exception) {
                        LogHelper.logDebug("Suppressed an error.")
                    }
                LogHelper.logDebug("${player.getPartyCapability().party?.leader?.displayName} -> ${player.getPartyCapability().party?.members?.joinToString { it.displayName }}")


                return null
            }
        }
    }
}
