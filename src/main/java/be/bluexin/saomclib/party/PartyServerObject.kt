package be.bluexin.saomclib.party

import be.bluexin.saomclib.SAOMCLib
import be.bluexin.saomclib.events.*
import be.bluexin.saomclib.message
import be.bluexin.saomclib.packets.party.Type
import be.bluexin.saomclib.packets.party.updateClient
import it.unimi.dsi.fastutil.objects.Object2LongLinkedOpenHashMap
import it.unimi.dsi.fastutil.objects.Object2LongMap
import net.minecraft.entity.player.EntityPlayerMP

class PartyServerObject (override var leaderInfo: PlayerInfo) : IParty {

    constructor(partyData: IPartyData): this(partyData.leaderInfo){
        membersInfo += partyData.membersInfo
        invitedInfo += partyData.invitedInfo
    }

    override val membersInfo: HashSet<PlayerInfo> = hashSetOf(leaderInfo)

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
                updateMembers(Type.JOIN, member)
                fireJoin(member)
                fireRefresh()
                return true
            }
            else if (isInvited(member)){
                invitedInfo -= member
                fireInviteCanceled(member)
                fireRefresh()
                updateMembers(Type.CANCELINVITE, member)
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
            if (membersInfo.count() <= 1) {
                dissolve()
            }
            else if (member == leaderInfo) {
                leaderInfo = run {
                    val leader = fireLeaderLeft()
                    if (leader != null && !membersInfo.contains(leader) && fireJoinCheck(leader)) {
                        fireJoin(leader)
                        fireRefresh()
                        membersInfo += leader
                    }
                    leader
                } ?: membersInfo.firstOrNull() ?: run {
                    dissolve()
                    leaderInfo
                }
                // if leader change wasn't possible, disband the party
                if (member == leaderInfo) return true
                fireLeaderChanged(leaderInfo, member)
                fireRefresh()
                updateMembers(Type.LEADERCHANGE, leaderInfo)
            }
            return true
        }
        else return false
    }

    private fun remove(player: PlayerInfo): Boolean {
        return membersInfo.remove(player)
        /*
        val memberIterator = membersInfo.iterator()
        var removed = false
        while (memberIterator.hasNext() && !removed){
            val member = memberIterator.next()
            if (member == player) {
                memberIterator.remove()
                Type.LEAVE.updateClient(player.player as EntityPlayerMP, this, player)
                updateMembers(Type.LEAVE, player)
                fireLeave(player)
                fireRefresh()
                removed = true
            }
        }
        return removed*/
    }

    override fun dissolve() {
        updateMembers(Type.DISBAND, PlayerInfo.EMPTY)
        fireDisbanded()
        membersInfo.clear()
        invitedInfo.clear()
        fireRefresh()
        PartyManager.removeParty(this)
    }

    override fun syncAll() {
        updateMembers(Type.REFRESH, PlayerInfo.EMPTY)
    }

    override fun sync(member: PlayerInfo) {
        Type.REFRESH.updateClient(member.player as EntityPlayerMP, this, PlayerInfo.EMPTY)
    }

    override fun invite(player: PlayerInfo): Boolean {
        if (player !in this && !isInvited(player) && fireInviteCheck(player)){
            // 300 second timer
            // TODO make timeout a config  option
            //invitedInfo += Pair(player, time + (300 * 20))
            invitedInfo += Pair(player, time + (20 * 20))
            leaderInfo.player?.message("commands.pt.invite.success", player.username)
            player.player?.message("commands.pt.invited", leaderInfo.username)
            updateMembers(Type.INVITE, player)
            fireInvited(player)
            fireRefresh()
            return true
        }
        return false
    }

    override fun cancel(player: PlayerInfo): Boolean {
        return if (invitedInfo.remove(player) != null){
            leaderInfo.player?.message("commands.pt.declined", player.username)
            player.player?.message("commands.pt.decline.success", leaderInfo.username)
            updateMembers(Type.CANCELINVITE, player)
            updateMember(Type.CANCELINVITE, player, player)
            fireInviteCanceled(player)
            if (!isParty)
                dissolve()
            else fireRefresh()
            true
        }
        else false
        /*
        val inviteIterator = invitedInfo.iterator()
        var removed = false
        while (inviteIterator.hasNext() && !removed){
            val invited = inviteIterator.next()
            if (invited.key == player) {
                inviteIterator.remove()
                leaderInfo.player?.message("commands.pt.declined", player.username)
                player.player?.message("commands.pt.decline.success", leaderInfo.username)
                updateMembers(Type.CANCELINVITE, player)
                updateMember(Type.CANCELINVITE, player, player)
                fireInviteCanceled(player)
                removed = true
            }
        }
        if (!isParty)
            dissolve()
        else fireRefresh()
        return removed*/
    }

    /**
     * Returns true if the party is no longer valid
     * and marks for removal
     */
    override fun cleanupInvites(): Boolean {
        val toRemove = arrayListOf<PlayerInfo>()
        invitedInfo.forEach {
            if (it.value <= time){
                toRemove.add(it.key)
            }
        }
        if (toRemove.isNotEmpty()){
            toRemove.forEach { player ->
                player.player?.message("commands.pt.cancel.notification")
                leaderInfo.player?.message("commands.pt.cancel.leaderNotification", player.username)
                invitedInfo.remove(player)
                updateMembers(Type.CANCELINVITE, player)
                updateMember(Type.CANCELINVITE, player, player)
                fireInviteCanceled(player)
            }
            fireRefresh()
        }
        return !isParty
    }

    fun updateMembers(type: Type, target: PlayerInfo){
        membersInfo.filter { it.player is EntityPlayerMP }.forEach { type.updateClient(it.player as EntityPlayerMP, this, target) }
        invitedInfo.filter { it.key.player is EntityPlayerMP }.forEach { type.updateClient(it.key.player as EntityPlayerMP, this, target) }
    }

    fun updateMember(type: Type, player: PlayerInfo, target: PlayerInfo){
        if (player.player != null && player.player is EntityPlayerMP){
            type.updateClient(player.player as EntityPlayerMP, this, target)
        }
    }

    companion object{
        val time: Long
            get() = SAOMCLib.proxy.getMainWorld().totalWorldTime
    }

}