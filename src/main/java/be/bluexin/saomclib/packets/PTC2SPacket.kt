package be.bluexin.saomclib.packets

import be.bluexin.saomclib.*
import be.bluexin.saomclib.capabilities.getPartyCapability
import cpw.mods.fml.common.network.simpleimpl.IMessage
import cpw.mods.fml.common.network.simpleimpl.MessageContext
import io.netty.buffer.ByteBuf
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.entity.player.EntityPlayerMP
import java.util.*

/**
 * Used to send party data
 * Client -> Server
 *
 * @author Bluexin
 */
class PTC2SPacket() : IMessage {

    private lateinit var type: Type
    private lateinit var member: String

    /**
     * @param type the type of action
     * @param member the player linked to the request, may be null depending on request
     */
    constructor(type: Type, member: EntityPlayer?) : this() {
        this.type = type
        this.member = member?.cachedUniqueIdString ?: ""
    }

    override fun fromBytes(buf: ByteBuf) {
        type = Type.values()[buf.readInt()]
        member = buf.readString()
    }

    override fun toBytes(buf: ByteBuf) {
        buf.writeInt(type.ordinal)
        buf.writeString(member)
    }

    enum class Type {
        REMOVE,
        INVITE,
        LEADER,
        JOIN,
        REQUEST, // TODO: change this from sync to "apply"-type stuff
        CANCEL
    }

    companion object {
        class Handler : AbstractServerPacketHandler<PTC2SPacket>() {
            override fun handleServerPacket(player: EntityPlayer, message: PTC2SPacket, ctx: MessageContext): IMessage? {
                try {
                    val cap = player.getPartyCapability()
                    val party = cap.getOrCreatePT()
                    val invitedTo = cap.invitedTo
                    if (party.leader == player) when (message.type) {
                        Type.REMOVE -> party.removeMember(player.world.getPlayerEntityByUUID(UUID.fromString(message.member))!!)
                        Type.INVITE -> party.invite(player.world.getPlayerEntityByUUID(UUID.fromString(message.member))!!)
                        Type.LEADER -> party.leader == player.world.getPlayerEntityByUUID(UUID.fromString(message.member))
                        Type.JOIN -> party.addMember(player.world.getPlayerEntityByUUID(UUID.fromString(message.member))!!)
                        Type.REQUEST -> (player as EntityPlayerMP).sendPacket(SyncEntityCapabilityPacket(player.getPartyCapability(), player))
                        Type.CANCEL -> party.cancel(player.world.getPlayerEntityByUUID(UUID.fromString(message.member))!!)
                    }
                    @Suppress("NON_EXHAUSTIVE_WHEN")
                    if (invitedTo?.isInvited(player) == true) when (message.type) {
                        Type.CANCEL -> invitedTo.cancel(player)
                        Type.JOIN -> invitedTo.addMember(player)
                    }
                } catch (e: Exception) {
                    SAOMCLib.LOGGER.debug("[PTC2SPacket] Suppressed an error.") // FIXME: very pro :ok_hand:
                    e.printStackTrace()
                }
                SAOMCLib.LOGGER.debug("${player.getPartyCapability().party?.leader?.displayNameString} -> ${player.getPartyCapability().party?.members?.joinToString { it.displayNameString }}")

                return null
            }

        }
    }
}