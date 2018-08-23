package be.bluexin.saomclib.party

import be.bluexin.saomclib.*
import be.bluexin.saomclib.capabilities.getPartyCapability
import be.bluexin.saomclib.events.PartyEvent
import be.bluexin.saomclib.packets.PTC2SPacket
import be.bluexin.saomclib.packets.PTS2CPacket
import be.bluexin.saomclib.packets.PacketPipeline
import be.bluexin.saomclib.packets.SyncEntityCapabilityPacket
import cpw.mods.fml.common.network.simpleimpl.IMessage
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.entity.player.EntityPlayerMP
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.nbt.NBTTagList
import net.minecraft.nbt.NBTTagString
import net.minecraft.world.World
import net.minecraftforge.common.MinecraftForge
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
                println("Adding ${member.displayNameString} to ${leader?.displayNameString}'s party.")
                membersImpl[member] = Unit
                invitesImpl.remove(member)
                val cap = member.getPartyCapability()
                if (cap.party != this) {
                    cap.party?.removeMember(member)
                    cap.party = this
                    cap.invitedTo = null
                }
                (member as EntityPlayerMP).sendPacket(PTS2CPacket(PTS2CPacket.Type.JOIN, leader!!, members))
                syncMembers()
                println("Done. Members: ${members.joinToString { it.displayNameString }}. Invited: ${invited.joinToString { it.displayNameString }}. Leader: ${leader?.displayNameString}")
            }
            world.get()?.onClient {
                membersImpl[member] = Unit
                invitesImpl.remove(member)
            }
            MinecraftForge.EVENT_BUS.post(PartyEvent.Join(this, member))
            return true
        }
        return false
    }

    override fun acceptInvite(player: EntityPlayer): Boolean {
        if (isInvited(player)) {
            world.get()?.onClient {
                PacketPipeline.sendToServer(PTC2SPacket(PTC2SPacket.Type.JOIN, player))
            }

            return true
        }

        return false
    }

    override fun removeMember(member: EntityPlayer) = if (membersImpl.remove(member) != null) {
        if (member == leader) leader = null
        world.get()?.onServer {
            member.getPartyCapability().clear()
            (member as EntityPlayerMP).sendPacket(PTS2CPacket(PTS2CPacket.Type.CLEAR, member, sequenceOf()))
            syncMembers()
        }
        world.get()?.onClient {
            PacketPipeline.sendToServer(PTC2SPacket(PTC2SPacket.Type.REMOVE, member))
        }
        MinecraftForge.EVENT_BUS.post(PartyEvent.Leave(this, member))
        if (!isParty) dissolve()
        true
    } else false

    override val members: Sequence<EntityPlayer>
        get() = membersImpl.keys.asSequence()

    override var leader: EntityPlayer?
        get() = leaderImpl?.get()
        set(player) {
            if (player == null) {
                @Suppress("RecursivePropertyAccessor")
                if (isParty) leader = members.first { it != leader }
            } else if (player in this && !isLeader(player)) {
                leaderImpl = WeakReference(player)
                world.get()?.onServer {
                    syncMembers()
                }
                world.get()?.onClient {
                    PacketPipeline.sendToServer(PTC2SPacket(PTC2SPacket.Type.LEADER, leaderImpl?.get()))
                }
                MinecraftForge.EVENT_BUS.post(PartyEvent.LeaderChanged(this, player))
            }
        }

    private var leaderImpl: WeakReference<EntityPlayer>? = null

    private val membersImpl = WeakHashMap<EntityPlayer, Unit>()

    private val invitesImpl = WeakHashMap<EntityPlayer, Long>()

    private val world: WeakReference<World>

    override fun dissolve() {
        println("Dissolving ${leader?.displayNameString}'s party.")
        members.forEach { it.getPartyCapability().clear() }
        if (leader != null) world.get()?.onServer { sendToMembers(PTS2CPacket(PTS2CPacket.Type.CLEAR, leader!!, sequenceOf())) }
        membersImpl.clear()
        MinecraftForge.EVENT_BUS.post(PartyEvent.Disbanded(this))
    }

    override val size: Int
        get() = membersImpl.size

    override val isParty: Boolean
        get() = size > 1

    override fun isMember(player: EntityPlayer) = membersImpl.contains(player)

    override fun invite(player: EntityPlayer): Boolean {
        if (!isMember(player) && !isInvited(player)) {
            player.getPartyCapability().invitedTo = this
            invitesImpl[player] = player.world.worldTime // TODO: Using worldTime isn't too safe
            world.get()?.onServer {
                println("Inviting ${player.displayNameString} to ${leader?.displayNameString}'s party.")
                (player as EntityPlayerMP).sendPacket(PTS2CPacket(PTS2CPacket.Type.INVITE, leader!!, members))
                syncMembers()
                println("Done. Members: ${members.joinToString { it.displayNameString }}. Invited: ${invited.joinToString { it.displayNameString }}. Leader: ${leader?.displayNameString}")
            }
            world.get()?.onClient {
                PacketPipeline.sendToServer(PTC2SPacket(PTC2SPacket.Type.INVITE, player))
            }
            MinecraftForge.EVENT_BUS.post(PartyEvent.Invited(this, player))
            return true
        }
        return false

    }

    override fun cancel(player: EntityPlayer) = if (invitesImpl.remove(player) != null) {
        world.get()?.onServer {
            (player as EntityPlayerMP).sendPacket(PTS2CPacket(PTS2CPacket.Type.CLEAR, player, sequenceOf()))
            syncMembers()
        }
        world.get()?.onClient {
            PacketPipeline.sendToServer(PTC2SPacket(PTC2SPacket.Type.CANCEL, player))
        }
        MinecraftForge.EVENT_BUS.post(PartyEvent.InviteCanceled(this, player))
        true
    } else false

    override fun isInvited(player: EntityPlayer) = invitesImpl.contains(player)

    override fun cleanupInvites(time: Long) {
        val remove = invitesImpl.filter { (if (time < it.value) time + 24000 else time) - it.value > 300 }
        remove.forEach { invitesImpl.remove(it.key) }
    }

    override val invited: Sequence<EntityPlayer>
        get() = invitesImpl.keys.asSequence()

    private fun sendToMembers(packet: IMessage) {
        world.get()?.onServer {
            members.forEach { (it as EntityPlayerMP).sendPacket(packet) }
        }
    }

    private fun syncMembers() {
        world.get()?.onServer {
            members.forEach { (it as EntityPlayerMP).sendPacket(SyncEntityCapabilityPacket(it.getPartyCapability(), it)) }
            invited.forEach { (it as EntityPlayerMP).sendPacket(SyncEntityCapabilityPacket(it.getPartyCapability(), it)) }
        }
    }

    override fun readNBT(nbt: NBTTagCompound) {
        val w = world.get() ?: return
        val sid = NBTTagString("").id.toInt()
        val membersTag = nbt.getTagList("members", sid)
        val invitesTag = nbt.getTagList("invites", sid)
        membersImpl.clear()
        membersTag.asSequence().mapNotNull {
            w.getPlayerEntityByUUID(UUID.fromString((it as NBTTagString).string))
        }.forEach {
            membersImpl[it] = Unit
        }
        invitesImpl.clear()
        invitesTag.asSequence().mapNotNull {
            world.get()?.getPlayerEntityByUUID(UUID.fromString((it as NBTTagString).string))
        }.forEach {
            invitesImpl[it] = w.worldTime
        }
        with(w.getPlayerEntityByUUID(UUID.fromString(nbt.getString("leader")))) {
            if (this != null) leaderImpl = WeakReference(this)
        }
        syncMembers()
    }

    override fun writeNBT(): NBTTagCompound {
        val membersTag = NBTTagList()
        members.forEach {
            membersTag.appendTag(NBTTagString(it.cachedUniqueIdString))
        }
        val invitesTag = NBTTagList()
        invited.forEach {
            invitesTag.appendTag(NBTTagString(it.cachedUniqueIdString))
        }

        val nbt = NBTTagCompound()
        nbt.setString("leader", leader?.cachedUniqueIdString ?: "")
        nbt.setTag("members", membersTag)
        nbt.setTag("invites", invitesTag)

        return nbt
    }

    init {
        membersImpl[leader] = Unit
        this.world = WeakReference(leader.world)
        this.leaderImpl = WeakReference(leader)
//        syncMembers()
    }

    override fun fixPostDeath(oldPlayer: EntityPlayer, newPlayer: EntityPlayer) {
        if (membersImpl.remove(oldPlayer) != null) membersImpl[newPlayer] = Unit
        val t = invitesImpl.remove(oldPlayer)
        if (t != null) invitesImpl[newPlayer] = t

        syncMembers()
    }
}
