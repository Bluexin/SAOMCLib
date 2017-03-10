package be.bluexin.saomclib.party

import be.bluexin.saomclib.capabilities.getPartyCapability
import be.bluexin.saomclib.onServer
import be.bluexin.saomclib.packets.PTPacket
import be.bluexin.saomclib.sendPacket
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
        if (!isMember(member)) {
            world.get()?.onServer {
                sendToMembers(PTPacket(PTPacket.Companion.Type.ADD, member, listOf()))
                (member as EntityPlayerMP).sendPacket(PTPacket(PTPacket.Companion.Type.JOIN, leader!!, members))
            }
            membersImpl.put(member, Unit)
            invitesImpl.remove(member)
            return true
        }

        return false
    }

    override fun removeMember(member: EntityPlayer) = if (membersImpl.remove(member) != null) {
        if (member == leader) leader = null
        world.get()?.onServer {
            sendToMembers(PTPacket(PTPacket.Companion.Type.REMOVE, member, listOf()))
            (member as EntityPlayerMP).sendPacket(PTPacket(PTPacket.Companion.Type.CLEAR, member, listOf()))
        }
        if (!isParty) dissolve()
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
            world.get()?.onServer { sendToMembers(PTPacket(PTPacket.Companion.Type.LEADER, leaderImpl!!.get(), listOf())) }
        }

    private var leaderImpl: WeakReference<EntityPlayer>? = null

    private val membersImpl = WeakHashMap<EntityPlayer, Unit>()

    private val invitesImpl = WeakHashMap<EntityPlayer, Long>()

    private val world: WeakReference<World>

    override fun dissolve() {
        members.forEach { it.getPartyCapability().clear() }
        if (leader != null) world.get()?.onServer { sendToMembers(PTPacket(PTPacket.Companion.Type.CLEAR, leader!!, listOf())) }
        membersImpl.clear()
    }

    override val size: Int
        get() = membersImpl.size

    override val isParty: Boolean
        get() = size > 1

    override fun isMember(player: EntityPlayer) = membersImpl.contains(player)

    override fun invite(player: EntityPlayer): Boolean {
        if (!isMember(player) && !isInvited(player)) {
            invitesImpl.put(player, player.world.worldTime) // TODO: Using worldTime isn't too safe
            world.get()?.onServer { (player as EntityPlayerMP).sendPacket(PTPacket(PTPacket.Companion.Type.INVITE, leader!!, members)) }
            return true
        }
        return false
    }

    override fun cancel(player: EntityPlayer) = if (invitesImpl.remove(player) != null) {
        world.get()?.onServer { (player as EntityPlayerMP).sendPacket(PTPacket(PTPacket.Companion.Type.CLEAR, player, listOf())) }
        true
    } else false

    override fun isInvited(player: EntityPlayer) = invitesImpl.contains(player)

    override fun cleanupInvites(time: Long) {
        val remove = invitesImpl.filter { (if (time < it.value) time + 24000 else time) - it.value > 300 }
        remove.forEach { invitesImpl.remove(it.key) }
    }

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
