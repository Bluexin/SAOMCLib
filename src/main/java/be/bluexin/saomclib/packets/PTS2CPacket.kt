package be.bluexin.saomclib.packets

import be.bluexin.saomclib.SAOMCLib
import be.bluexin.saomclib.capabilities.getPartyCapability
import be.bluexin.saomclib.party.Party
import be.bluexin.saomclib.readString
import be.bluexin.saomclib.writeString
import io.netty.buffer.ByteBuf
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.util.IThreadListener
import net.minecraftforge.fml.common.network.simpleimpl.IMessage
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext
import java.util.*

/**
 * Part of saomclib by Bluexin.
 *
 * @author Bluexin
 */
class PTS2CPacket() : IMessage {

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
        this.leader = leader.cachedUniqueIdString
        this.members = members.map { it.cachedUniqueIdString }
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

        class Handler : AbstractClientPacketHandler<PTS2CPacket>() {
            override fun handleClientPacket(player: EntityPlayer, message: PTS2CPacket, ctx: MessageContext, mainThread: IThreadListener): IMessage? {
                mainThread.addScheduledTask {
                    val p1 = player.world.getPlayerEntityByUUID(UUID.fromString(message.leader))
                    SAOMCLib.LOGGER.info("${player.displayNameString} received ${message.type} with p1 ${p1?.displayNameString}")
                    try {
                        if (p1 != null){
                            when (message.type) {
                                Type.ADD -> player.getPartyCapability().getOrCreatePT().addMember(p1)
                                Type.REMOVE -> player.getPartyCapability().party?.removeMember(p1)
                                Type.CLEAR -> player.getPartyCapability().clear()
                                Type.INVITE -> {
                                    val pt = Party(p1)
                                    message.members.mapNotNull { player.world.getPlayerEntityByUUID(UUID.fromString(it)) }.forEach { pt.addMember(it) }
                                    player.getPartyCapability().invitedTo = pt

                                }
                                Type.LEADER -> player.getPartyCapability().party?.leader = p1
                                Type.JOIN -> {
                                    val pt = Party(p1)
                                    message.members.mapNotNull { player.world.getPlayerEntityByUUID(UUID.fromString(it)) }.forEach { pt.addMember(it) }
                                    pt.addMember(player)
                                    val cap = player.getPartyCapability()
                                    cap.party = pt
                                    cap.invitedTo = null

                                }
                            }
                        }
                    } catch (e: Exception) {
                        SAOMCLib.LOGGER.debug("Suppressed an error.")
                    }
                    SAOMCLib.LOGGER.debug("${player.getPartyCapability().party?.leader?.displayNameString} -> ${player.getPartyCapability().party?.members?.joinToString { it.displayNameString }}")
                }

                return null
            }
        }
    }
}
