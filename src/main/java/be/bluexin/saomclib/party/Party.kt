package be.bluexin.saomclib.party

import be.bluexin.saomclib.SAOMCLib
import be.bluexin.saomclib.capabilities.getPartyCapability
import be.bluexin.saomclib.events.*
import be.bluexin.saomclib.onClient
import be.bluexin.saomclib.onServer
import be.bluexin.saomclib.packets.ClearPartyPacket
import be.bluexin.saomclib.packets.PacketPipeline
import be.bluexin.saomclib.packets.PartyPacket
import be.bluexin.saomclib.packets.SyncEntityCapabilityPacket
import be.bluexin.saomclib.sendPacket
import it.unimi.dsi.fastutil.objects.Object2LongLinkedOpenHashMap
import it.unimi.dsi.fastutil.objects.Object2LongMap
import net.minecraft.entity.player.EntityPlayerMP
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.nbt.NBTTagList
import net.minecraft.nbt.NBTTagString
import java.util.*

// TODO:  /partywarp <player> <dimension> <x> <y> <z>
class Party(leader: IPlayerInfo) : IParty {

    override fun addMember(member: IPlayerInfo): Boolean {
        if (leaderInfo != null && member !in this) {
            // Ensure leader is in party member list.
            membersImpl.add(leaderInfo!!)
            membersImpl += member
            invitedImpl -= member
            world?.onServer {
                println("Adding ${member.username} to ${leaderInfo?.username}'s party.")
                val cap = member.player?.getPartyCapability()
                if (cap?.party != this) {
                    cap!!.party?.removeMember(member)
                    cap.party = this
                    cap.invitedTo = null
                }
                this.syncMembers()
                println("Done. Members: ${membersInfo.joinToString { it.username }}. Invited: ${invitedInfo.joinToString { it.key.username }}. Leader: ${leaderInfo?.username}")

                fireJoin(member)
            }
            return true
        }
        return false
    }

    override fun acceptInvite(player: IPlayerInfo): Boolean {
        if (this.isInvited(player)) {
            world?.onClient {
                PacketPipeline.sendToServer(PartyPacket(PartyPacket.Type.JOIN, player.uuidString))
            }

            return true
        }

        return false
    }

    override fun removeMember(member: IPlayerInfo) = if (membersImpl.remove(member)) {
        if (member == leaderInfo) leaderInfo = null
        world?.onServer {
            val player = member.player
            if (player != null) {
                player.getPartyCapability().clear()
                (player as EntityPlayerMP).sendPacket(ClearPartyPacket(ClearPartyPacket.Type.PARTY))
            }
            syncMembers()
        }
        if (membersImpl.isNotEmpty()) {
            world?.onClient {
                PacketPipeline.sendToServer(PartyPacket(PartyPacket.Type.REMOVE, member.uuidString))
            }
            world?.onServer {
                fireLeave(member)
            }
        }
        if (!isParty) dissolve()
        true
    } else false

    private val membersImpl: MutableCollection<IPlayerInfo> = hashSetOf()
    private val invitedImpl: Object2LongMap<IPlayerInfo> = Object2LongLinkedOpenHashMap<IPlayerInfo>().apply {
        defaultReturnValue(Long.MIN_VALUE)
    }

    override val membersInfo: Sequence<IPlayerInfo>
        get() = membersImpl.asSequence()

    override val invitedInfo: Sequence<Object2LongMap.Entry<IPlayerInfo>>
        get() = invitedImpl.object2LongEntrySet().asSequence()

    override var leaderInfo: IPlayerInfo? = leader
        set(player) {
            if (player == null) {
                @Suppress("RecursivePropertyAccessor")
                if (isParty) leaderInfo = membersInfo.first { it != leaderInfo }
            } else if (player in this && !isLeader(player)) {
                field = player
                world?.onServer {
                    syncMembers()
                    fireLeaderChanged(player)
                }
                world?.onClient {
                    PacketPipeline.sendToServer(PartyPacket(PartyPacket.Type.LEADER, player.uuidString))
                }
            }
        }

    private val world get() = leaderInfo?.player?.world

    override fun dissolve() {
        SAOMCLib.LOGGER.info("Dissolving ${leaderInfo?.username}'s party.")
        membersInfo.forEach { it.player?.getPartyCapability()?.clear() }
        world?.onServer {
            disband()
        }
        membersImpl.clear()
        world?.onServer {
            fireDisbanded()
        }
    }

    override val size: Int
        get() = membersImpl.size

    override val isParty: Boolean
        get() = leaderInfo != null && membersImpl.any { it != leaderInfo }

    override fun isMember(player: IPlayerInfo) = player in this.membersImpl

    override fun invite(player: IPlayerInfo): Boolean {
        if (!isMember(player) && !isInvited(player)) {
            player.player?.getPartyCapability()?.invitedTo = this
            @Suppress("ReplacePutWithAssignment") // would introduce boxing
            invitedImpl.put(player, this.world?.worldTime ?: 0) // TODO: Using worldTime isn't too safe
            val w = this.world
            w?.onServer {
                SAOMCLib.LOGGER.info("Inviting ${player.username} to ${leaderInfo?.username}'s party.")
                syncMembers()
                fireInvited(player)
            }
            w?.onClient {
                PacketPipeline.sendToServer(PartyPacket(PartyPacket.Type.INVITE, player.uuidString))
            }
            return true
        }
        return false
    }

    override fun cancel(player: IPlayerInfo) = if (invitedImpl.removeLong(player) != Long.MIN_VALUE) {
        world?.onServer {
            fireInviteCanceled(player)
            (player.player as? EntityPlayerMP)?.sendPacket(ClearPartyPacket(ClearPartyPacket.Type.INVITE))
            syncMembers()
        }
        world?.onClient {
            PacketPipeline.sendToServer(PartyPacket(PartyPacket.Type.CANCEL, player.uuidString))
        }
        true
    } else false

    override fun isInvited(player: IPlayerInfo) = player in this.invitedImpl

    override fun cleanupInvites(time: Long) {
        val remove = invitedInfo.filter { (if (time < it.longValue) time + 24000 else time) - it.longValue > 300 }
        remove.forEach { invitedImpl.removeLong(it.key) }
    }

    override fun readNBT(nbt: NBTTagCompound) {
        val w = world ?: return
        val membersTag = nbt.getTagList("members", NBTTagString("").id.toInt())
        val invitesTag = nbt.getTagList("invites", NBTTagCompound().id.toInt())
        var leader: IPlayerInfo? = null
        val leaderUUID = nbt.getString("leader")
        membersImpl.clear()
        membersTag.forEach {
            val uuid = (it as NBTTagString).string
            val player = PlayerInfo(UUID.fromString(uuid), w)
            membersImpl += player
            if (uuid == leaderUUID) leader = player
        }
        if (leader != null) this.leaderInfo = leader
        invitedImpl.clear()
        invitesTag.forEach {
            it as NBTTagCompound
            @Suppress("ReplacePutWithAssignment") // would introduce boxing
            invitedImpl.put(PlayerInfo(
                    UUID.fromString(it.getString("uuid")), w),
                    it.getLong("time")
            )
        }
        world?.onServer {
            fireRefreshed()
        }
    }

    override fun writeNBT(): NBTTagCompound {
        val membersTag = NBTTagList()
        membersInfo.forEach {
            membersTag.appendTag(NBTTagString(it.uuidString))
        }
        val invitesTag = NBTTagList()
        var inv: NBTTagCompound
        invitedInfo.forEach {
            inv = NBTTagCompound()
            inv.setString("uuid", it.key.uuidString)
            inv.setLong("time", it.longValue)
            invitesTag.appendTag(inv)
        }

        val nbt = NBTTagCompound()
        nbt.setString("leader", leaderInfo?.uuidString ?: "")
        nbt.setTag("members", membersTag)
        nbt.setTag("invites", invitesTag)

        return nbt
    }

    init {
        this.membersImpl += leader
        this.syncMembers()
    }

    private fun syncMembers() {
        world?.onServer {
            membersInfo.mapNotNull { it.player as? EntityPlayerMP }.forEach { it.sendPacket(SyncEntityCapabilityPacket(it.getPartyCapability(), it)) }
            invitedInfo.mapNotNull { it.key.player as? EntityPlayerMP }.forEach { it.sendPacket(SyncEntityCapabilityPacket(it.getPartyCapability(), it)) }
        }
    }

    private fun disband() {
        world?.onServer {
            membersInfo.mapNotNull { it.player as? EntityPlayerMP }.forEach { it.sendPacket(ClearPartyPacket(ClearPartyPacket.Type.PARTY)) }
            invitedInfo.mapNotNull { it.key.player as? EntityPlayerMP }.forEach { it.sendPacket(ClearPartyPacket(ClearPartyPacket.Type.INVITE)) }
        }
    }

}