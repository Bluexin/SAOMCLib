package be.bluexin.saomclib.party

import be.bluexin.saomclib.events.fireLeaderChanged
import net.minecraft.entity.player.EntityPlayer
import java.util.*

/**
 * Part of saomclib.
 * Stores a party and everything related to using it.
 *
 * @author Bluexin
 */
interface IParty: IPartyData {

    /**
     * Adds a member to this party.
     * Reaction to already present member is up to implementation details.
     *
     * Should fire [PartyEventV3.Join] when successful, both on client and server.
     *
     * @param member the member to add
     * @return whether the operation was successful (typically whether the player was already present, or following hypothetical size limit)
     */
    fun addMember(member: EntityPlayer): Boolean = addMember(PlayerInfo(member))

    fun addMember(member: UUID): Boolean = addMember(PlayerInfo(member))

    fun addMember(member: PlayerInfo): Boolean

    /**
     * Accepts a party invite.
     *
     * @param player the player accepting the invite
     * @return whether the operation was successful
     */
    fun acceptInvite(player: EntityPlayer): Boolean = acceptInvite(PlayerInfo(player))

    fun acceptInvite(player: UUID): Boolean = acceptInvite(PlayerInfo(player))

    fun acceptInvite(player: PlayerInfo): Boolean

    /**
     * Removes a member from this party.
     *
     * Should fire [PartyEvent3.Leave] when successful, both on client and server.
     *
     * @param member the member to remove
     * @return whether the operation was successful
     */
    fun removeMember(member: EntityPlayer): Boolean = removeMember(PlayerInfo(member))

    fun removeMember(member: UUID): Boolean = removeMember(PlayerInfo(member))

    fun removeMember(member: PlayerInfo): Boolean

    /**
     * Dissolves this party, aka removing all the members.
     *
     * Should fire [PartyEvent3.Disbanded] when successful, both on client and server.
     */
    fun dissolve()


    /**
     * Invite someone to this party.
     *
     * Should fire [PartyEvent3.Invited] when successful, both on client and server.
     *
     * @param player the player to invite
     */
    fun invite(player: EntityPlayer): Boolean = invite(PlayerInfo(player))

    fun invite(player: UUID): Boolean = invite(PlayerInfo(player))

    fun invite(player: PlayerInfo): Boolean

    /**
     * Cancel a party invite.
     *
     * Should fire [PartyEvent3.InviteCanceled] when successful, both on client and server.
     *
     * @param player the player who's invite is to cancel
     * @return whether the cancel was successful
     */
    fun cancel(player: EntityPlayer): Boolean = cancel(PlayerInfo(player))

    fun cancel(player: UUID): Boolean = cancel(PlayerInfo(player))

    fun cancel(player: PlayerInfo): Boolean

    /**
     * Clean up the invites (invite timeout) based on current world time.
     *
     * @param time the current world time
     */
    fun cleanupInvites(time: Long)


    /**
     * Changes the current leader, returns false if new
     * leader isn't in the current party.
     */
    fun changeLeader(player: EntityPlayer): Boolean = changeLeader(PlayerInfo(player))

    fun changeLeader(player: UUID): Boolean = changeLeader(PlayerInfo(player))

    fun changeLeader(player: PlayerInfo): Boolean{
        if (player !in this)
            return false
        leaderInfo = player
        fireLeaderChanged(player)
        return true
    }
}
