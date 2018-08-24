package be.bluexin.saomclib.packets

import be.bluexin.saomclib.SAOMCLib
import be.bluexin.saomclib.capabilities.getPartyCapability
import be.bluexin.saomclib.readString
import be.bluexin.saomclib.sendPacket
import be.bluexin.saomclib.writeString
import io.netty.buffer.ByteBuf
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.entity.player.EntityPlayerMP
import net.minecraft.util.IThreadListener
import net.minecraftforge.fml.common.network.simpleimpl.IMessage
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext
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
            override fun handleServerPacket(player: EntityPlayer, message: PTC2SPacket, ctx: MessageContext, mainThread: IThreadListener): IMessage? {
                mainThread.addScheduledTask {
                    try {
                        val cap = player.getPartyCapability()
                        val party = cap.getOrCreatePT()
                        val invitedTo = cap.invitedTo
                        @Suppress("NON_EXHAUSTIVE_WHEN") if (party.leader == player) when (message.type) {
                            Type.REMOVE -> party.removeMember(player.world.getPlayerEntityByUUID(UUID.fromString(message.member))!!)
                            Type.INVITE -> party.invite(player.world.getPlayerEntityByUUID(UUID.fromString(message.member))!!)
                            Type.LEADER -> party.leader == player.world.getPlayerEntityByUUID(UUID.fromString(message.member))
                            Type.JOIN -> party.addMember(player.world.getPlayerEntityByUUID(UUID.fromString(message.member))!!)
                            Type.REQUEST -> (player as EntityPlayerMP).sendPacket(SyncEntityCapabilityPacket(player.getPartyCapability(), player))
                            Type.CANCEL -> party.cancel(player.world.getPlayerEntityByUUID(UUID.fromString(message.member))!!)
                        } else if (invitedTo?.isInvited(player) == true) when (message.type) {
                            Type.CANCEL -> invitedTo.cancel(player)
                            Type.JOIN -> invitedTo.addMember(player)

                        }
                    } catch (e: Exception) {
                        SAOMCLib.LOGGER.debug("[PTC2SPacket] Suppressed an error.") // FIXME: very pro :ok_hand:
                        e.printStackTrace()
                    }
                }

                return null
            }

        }
    }
}
