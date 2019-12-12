package be.bluexin.saomclib.party

import be.bluexin.saomclib.SAOMCLib
import be.bluexin.saomclib.events.*
import be.bluexin.saomclib.packets.party.Type
import be.bluexin.saomclib.packets.party.updateClient
import it.unimi.dsi.fastutil.objects.Object2LongLinkedOpenHashMap
import it.unimi.dsi.fastutil.objects.Object2LongMap
import net.minecraft.entity.player.EntityPlayerMP
import java.util.*

class PartyObject(override var leaderInfo: PlayerInfo) : IParty {

    constructor(partyData: IPartyData): this(partyData.leaderInfo){
        membersInfo += partyData.membersInfo
        invitedInfo += partyData.invitedInfo
    }

    override val membersInfo: MutableCollection<PlayerInfo> = hashSetOf(leaderInfo)

    override val invitedInfo: Object2LongMap<PlayerInfo> = Object2LongLinkedOpenHashMap<PlayerInfo>().apply {
        defaultReturnValue(Long.MIN_VALUE)
    }

    /**
     * Attempts to add player to the current party
     * If the join check fails and the player is
     * invited, the invite will be canceled
     */
    override fun addMember(member: PlayerInfo): Boolean {
        if (member !in this){
            if (fireJoinCheck(member)) {
                membersInfo += leaderInfo
                membersInfo += member
                invitedInfo -= member
                updateMembers(Type.JOIN, member.uuid)
                fireJoin(member)
                fireRefresh()
                return true
            }
            else if (isInvited(member)){
                invitedInfo -= member
                fireInviteCanceled(member)
                fireRefresh()
                updateMembers(Type.CANCELINVITE, member.uuid)
            }

        }
        return false
    }

    override fun acceptInvite(player: PlayerInfo): Boolean {
        return if (isInvited(player)){
            addMember(player)
            true
        } else false
    }

    override fun removeMember(member: PlayerInfo): Boolean{
        if (remove(member)){
            if (membersInfo.count() <= 1 || membersInfo.isEmpty()) {
                dissolve()
            }
            else if (member == leaderInfo) {
                leaderInfo = {
                    val leader = fireLeaderLeft()
                    if (leader != null && !membersInfo.contains(leader) && fireJoinCheck(leader)) {
                        fireJoin(leader)
                        fireRefresh()
                        membersInfo += leader
                    }
                    leader
                }.invoke() ?: membersInfo.firstOrNull() ?: {
                    dissolve()
                    leaderInfo
                }.invoke()
                // if leader change wasn't possible, disband the party
                if (member == leaderInfo) return true
                fireLeaderChanged(leaderInfo)
                fireRefresh()
                updateMembers(Type.LEADERCHANGE, leaderInfo.uuid)
            }
            return true
        }
        else return false
    }

    private fun remove(player: PlayerInfo) = if (membersInfo.remove(player)){
        Type.LEAVE.updateClient(player.player as EntityPlayerMP, this, player.uuid)
        updateMembers(Type.LEAVE, player.uuid)
        fireLeave(player)
        fireRefresh()
        true
    } else false

    override fun dissolve() {
        updateMembers(Type.DISBAND, null)
        fireDisbanded()
        membersInfo.clear()
        invitedInfo.clear()
        fireRefresh()
        PartyManager.removeParty(this)
    }

    override fun invite(player: PlayerInfo): Boolean {
        if (player !in this && !isInvited(player) && fireInviteCheck(player)){
            // 300 second timer
            // TODO make timeout a config  option
            invitedInfo += Pair(player, SAOMCLib.proxy.getMainWorld().totalWorldTime + (300 * 20))
            updateMembers(Type.INVITE, player.uuid)
            fireInvited(player)
            fireRefresh()
            return true
        }
        return false
    }

    override fun cancel(player: PlayerInfo) = if (invitedInfo.remove(player) != null) {
        invitedInfo -= player
        updateMembers(Type.CANCELINVITE, player.uuid)
        fireInviteCanceled(player)
        fireRefresh()
        true
    } else false

    override fun cleanupInvites(time: Long) {
        invitedInfo.object2LongEntrySet().removeAll {
            if (it.longValue <= time) {
                updateMembers(Type.CANCELINVITE, it.key.uuid)
                fireInviteCanceled(it.key)
                fireRefresh()
                true
            } else false
        }
        if (!isParty) dissolve()
    }

    fun updateMembers(type: Type, target: UUID?){
        membersInfo.asSequence().filter { it.player is EntityPlayerMP }.forEach { type.updateClient(it.player as EntityPlayerMP, this, target) }
        invitedInfo.asSequence().filter { it.key.player is EntityPlayerMP }.forEach { type.updateClient(it.key.player as EntityPlayerMP, this, target) }
    }

}