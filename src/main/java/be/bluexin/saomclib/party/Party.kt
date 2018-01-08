package be.bluexin.saomclib.party

import be.bluexin.saomclib.capabilities.getPartyCapability
import be.bluexin.saomclib.onClient
import be.bluexin.saomclib.onServer
import be.bluexin.saomclib.packets.PTC2SPacket
import be.bluexin.saomclib.packets.PTS2CPacket
import be.bluexin.saomclib.packets.PacketPipeline
import be.bluexin.saomclib.packets.SyncEntityCapabilityPacket
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
                membersImpl.put(member, Unit)
                invitesImpl.remove(member)
                val cap = member.getPartyCapability()
                cap.party?.removeMember(member)
                cap.party = this
                cap.invitedTo = null
                (member as EntityPlayerMP).sendPacket(PTS2CPacket(PTS2CPacket.Type.JOIN, leader!!, members))
                syncMembers()
            }
            return true
        }

        return false

    }

    override fun removeMember(member: EntityPlayer) = if (membersImpl.remove(member) != null) {
        if (member == leader) leader = null
        world.get()?.onServer {
            (member as EntityPlayerMP).sendPacket(PTS2CPacket(PTS2CPacket.Type.CLEAR, member, listOf()))
            syncMembers()
        }
        world.get()?.onClient {
            PacketPipeline.sendToServer(PTC2SPacket(PTC2SPacket.Type.REMOVE, member))
        }
        if (!isParty) dissolve()
        true
    } else false

    override val members: List<EntityPlayer>
        get() = membersImpl.keys.toList()

    override var leader: EntityPlayer?
        get() = leaderImpl?.get()
        set(player) {
            if (player == null) {
                @Suppress("RecursivePropertyAccessor")
                if (isParty) leader = membersImpl.keys.first()
            } else if (player in this && !isLeader(player)) {
                leaderImpl = WeakReference(player)
                world.get()?.onServer {
                    syncMembers()
                }
                world.get()?.onClient {
                    PacketPipeline.sendToServer(PTC2SPacket(PTC2SPacket.Type.LEADER, leaderImpl?.get()))
                }
            }
        }

    private var leaderImpl: WeakReference<EntityPlayer>? = null

    private val membersImpl = WeakHashMap<EntityPlayer, Unit>()

    private val invitesImpl = WeakHashMap<EntityPlayer, Long>()

    private val world: WeakReference<World>

    override fun dissolve() {
        members.forEach { it.getPartyCapability().clear() }
        if (leader != null) world.get()?.onServer { sendToMembers(PTS2CPacket(PTS2CPacket.Type.CLEAR, leader!!, listOf())) }
        membersImpl.clear()
    }

    override val size: Int
        get() = membersImpl.size

    override val isParty: Boolean
        get() = size > 1

    override fun isMember(player: EntityPlayer) = membersImpl.contains(player)

    override fun invite(player: EntityPlayer): Boolean {
        if (!isMember(player) && !isInvited(player)) {
            player.getPartyCapability().invitedTo = this
            invitesImpl.put(player, player.world.worldTime) // TODO: Using worldTime isn't too safe
            world.get()?.onServer {
                (player as EntityPlayerMP).sendPacket(PTS2CPacket(PTS2CPacket.Type.INVITE, leader!!, members))
                syncMembers()
            }
            world.get()?.onClient {
                PacketPipeline.sendToServer(PTC2SPacket(PTC2SPacket.Type.INVITE, player))
            }
            return true
        }
        return false

    }

    override fun cancel(player: EntityPlayer) = if (invitesImpl.remove(player) != null) {
        world.get()?.onServer {
            (player as EntityPlayerMP).sendPacket(PTS2CPacket(PTS2CPacket.Type.CLEAR, player, listOf()))
            syncMembers()
        }
        world.get()?.onClient {
            PacketPipeline.sendToServer(PTC2SPacket(PTC2SPacket.Type.CANCEL, player))
        }
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

    /**
     * Only call on server!
     */
    private fun syncMembers() {
        members.forEach { (it as EntityPlayerMP).sendPacket(SyncEntityCapabilityPacket(it.getPartyCapability(), it)) }
    }

    init {
        membersImpl.put(leader, Unit)
        this.world = WeakReference(leader.world)
        this.leader = leader
    }
}
