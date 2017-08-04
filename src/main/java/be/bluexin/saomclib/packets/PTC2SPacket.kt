package be.bluexin.saomclib.packets

import be.bluexin.saomclib.SAOMCLib
import be.bluexin.saomclib.capabilities.getPartyCapability
import be.bluexin.saomclib.readString
import be.bluexin.saomclib.writeString
import io.netty.buffer.ByteBuf
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.util.IThreadListener
import net.minecraftforge.fml.common.network.simpleimpl.IMessage
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext
import java.util.*

class PTC2SPacket(): IMessage {

    private lateinit var type: Type
    private lateinit var player: String
    private lateinit var member: String

    /**
     * @param type the type of action
     * @param player the player sending the request
     * @param member the player linked to the request, may be null depending on request
     */
    constructor(type: Type, player: EntityPlayer, member: EntityPlayer?) : this() {
        this.type = type
        this.player = player.cachedUniqueIdString
        this.member = member?.cachedUniqueIdString ?: ""
    }

    override fun fromBytes(buf: ByteBuf) {
        type = Type.values()[buf.readInt()]
        player = buf.readString()
        member = buf.readString()
    }

    override fun toBytes(buf: ByteBuf) {
        buf.writeInt(type.ordinal)
        buf.writeString(player)
        buf.writeString(member)
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

        class Handler : AbstractServerPacketHandler<PTC2SPacket>() {
            override fun handleServerPacket(player: EntityPlayer, message: PTC2SPacket, ctx: MessageContext, mainThread: IThreadListener): IMessage? {
                mainThread.addScheduledTask {
                    val p1 = player.world.getPlayerEntityByUUID(UUID.fromString(message.player))
                    SAOMCLib.LOGGER.info("${player.displayNameString} received ${message.type} with p1 ${p1?.displayNameString}")
                    try {
                        if (p1 != null) {
                            val party = p1.getPartyCapability().getOrCreatePT()
                            when (message.type) {
                                Type.ADD -> party.addMember(player.world.getPlayerEntityByUUID(UUID.fromString(message.member))!!)
                                Type.REMOVE -> party.removeMember(player.world.getPlayerEntityByUUID(UUID.fromString(message.member))!!)
                                Type.CLEAR -> p1.getPartyCapability().clear()
                                Type.INVITE -> party.invite(player.world.getPlayerEntityByUUID(UUID.fromString(message.member))!!)
                                Type.LEADER -> party.leader == p1
                                Type.JOIN -> party.addMember(player.world.getPlayerEntityByUUID(UUID.fromString(message.member))!!)
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
