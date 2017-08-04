package be.bluexin.saomclib.party

import be.bluexin.saomclib.capabilities.getPartyCapability
import be.bluexin.saomclib.onClient
import be.bluexin.saomclib.onServer
import be.bluexin.saomclib.packets.PTC2SPacket
import be.bluexin.saomclib.packets.PTS2CPacket
import be.bluexin.saomclib.sendPacket
import be.bluexin.saomclib.sentPacketToServer
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.entity.player.EntityPlayerMP
import net.minecraft.world.World
import net.minecraftforge.fml.common.network.simpleimpl.IMessage
import java.lang.ref.WeakReference
import java.util.*

/**
 * Part of saomclib by Bluexin.
 *
 * @author Bluexin
 */
class Party(leader: EntityPlayer) : IParty {

    override fun addMember(member: EntityPlayer): Boolean {
        world.get()?.onServer {
            if (!isMember(member)) {
                member.getPartyCapability().party?.removeMember(member)
                sendToMembers(PTS2CPacket(PTS2CPacket.Companion.Type.ADD, member, listOf()))
                (member as EntityPlayerMP).sendPacket(PTS2CPacket(PTS2CPacket.Companion.Type.JOIN, leader!!, members))
                membersImpl.put(member, Unit)
                invitesImpl.remove(member)
                return true
            }
            return false
        }
        world.get()?.onClient {
            leader!!.sentPacketToServer(PTC2SPacket(PTC2SPacket.Companion.Type.JOIN, leader!!, member))
            return true
        }

        return false
    }

    override fun removeMember(member: EntityPlayer) = if (membersImpl.remove(member) != null) {
        world.get()?.onServer {
            if (member == leader) leader = null
            sendToMembers(PTS2CPacket(PTS2CPacket.Companion.Type.REMOVE, member, listOf()))
            (member as EntityPlayerMP).sendPacket(PTS2CPacket(PTS2CPacket.Companion.Type.CLEAR, member, listOf()))

            if (!isParty) dissolve()
        }
        world.get()?.onClient {
            leader!!.sentPacketToServer(PTC2SPacket(PTC2SPacket.Companion.Type.REMOVE, leader!!, member))
        }

        true
    } else false

    override val members: List<EntityPlayer>
        get() = membersImpl.keys.toList()

    override var leader: EntityPlayer?
        get() = leaderImpl?.get()
        set(value) {
            if (value == null) {
                if (isParty) leaderImpl = WeakReference(membersImpl.keys.first())
            } else if (isMember(value)) leaderImpl = WeakReference(value)
            else throw IllegalStateException("Target to be promoted isn't in the party!")
            world.get()?.onServer { sendToMembers(PTS2CPacket(PTS2CPacket.Companion.Type.LEADER, leaderImpl!!.get()!!, listOf())) }
            world.get()?.onClient { leader!!.sentPacketToServer(PTC2SPacket(PTC2SPacket.Companion.Type.LEADER, leaderImpl!!.get()!!, null)) }
        }

    private var leaderImpl: WeakReference<EntityPlayer>? = null

    private val membersImpl = WeakHashMap<EntityPlayer, Unit>()

    private val invitesImpl = WeakHashMap<EntityPlayer, Long>()

    private val world: WeakReference<World>

    override fun dissolve() {
        world.get()?.onServer {
            members.forEach { it.getPartyCapability().clear() }
            if (leader != null) sendToMembers(PTS2CPacket(PTS2CPacket.Companion.Type.CLEAR, leader!!, listOf()))
            membersImpl.clear()
        }
        world.get()?.onClient { leader!!.sentPacketToServer(PTC2SPacket(PTC2SPacket.Companion.Type.CLEAR, leader!!, null)) }
    }

    override val size: Int
        get() = membersImpl.size

    override val isParty: Boolean
        get() = size > 1

    override fun isMember(player: EntityPlayer) = membersImpl.contains(player)

    override fun invite(player: EntityPlayer): Boolean {
        world.get()?.onServer {
            if (!isMember(player) && !isInvited(player)) {
                invitesImpl.put(player, player.world.worldTime) // TODO: Using worldTime isn't too safe
                (player as EntityPlayerMP).sendPacket(PTS2CPacket(PTS2CPacket.Companion.Type.INVITE, leader!!, members))
                return true
            }
            else return false
        }
        world.get()?.onClient {
            leader!!.sentPacketToServer(PTC2SPacket(PTC2SPacket.Companion.Type.INVITE, leader!!, player))
            return true
        }
        return false
    }

    override fun cancel(player: EntityPlayer) = if (invitesImpl.remove(player) != null) {
        world.get()?.onServer { (player as EntityPlayerMP).sendPacket(PTS2CPacket(PTS2CPacket.Companion.Type.CLEAR, player, listOf())) }
        world.get()?.onClient { player.sentPacketToServer(PTC2SPacket(PTC2SPacket.Companion.Type.CLEAR, player, null)) }
        true
    } else false

    override fun isInvited(player: EntityPlayer) = invitesImpl.contains(player)

    override fun cleanupInvites(time: Long) {
        val remove = invitesImpl.filter { (if (time < it.value) time + 24000 else time) - it.value > 300 }
        remove.forEach { invitesImpl.remove(it.key) }
    }

    override val invited: List<EntityPlayer>
        get() = invitesImpl.keys.toList()

    /**
     * Only call on server!
     */
    private fun sendToMembers(packet: IMessage) {
        members.forEach { (it as EntityPlayerMP).sendPacket(packet) }
    }

    init {
        membersImpl.put(leader, Unit)
        this.world = WeakReference(leader.world)
        this.leader = leader
    }
}
