package be.bluexin.saomclib.party

import net.minecraft.entity.player.EntityPlayer
import net.minecraft.nbt.NBTTagCompound

/**
 * Part of saomclib.
 * Stores a party and everything related to using it.
 *
 * @author Bluexin
 */
interface IParty {

    /**
     * Adds a member to this party.
     * Reaction to already present member is up to implementation details.
     *
     * @param member the member to add
     * @return whether the operation was successful (typically whether the player was already present, or following hypothetical size limit)
     */
    fun addMember(member: EntityPlayer): Boolean

    /**
     * Removes a member from this party.
     *
     * @param member the member to remove
     * @return whether the operation was successful
     */
    fun removeMember(member: EntityPlayer): Boolean

    /**
     * Gets a sequence containing all the members of this party.
     * Returned sequence safety (concurrency, mutability, ...) is up to implementation details.
     *
     * @return all the members in this party
     */
    val members: Sequence<EntityPlayer>

    /**
     * Gets the leader of this party.
     * Returned reference safety (concurrency, mutability, ...) is up to implementation details.
     *
     * @return the leader of this party.
     */
    var leader: EntityPlayer?

    /**
     * Dissolves this party, aka removing all the members.
     */
    fun dissolve()

    /**
     * Gets the size of this party, aka the amount of members.
     *
     * @return the amount of members in this party
     */
    val size: Int

    /**
     * Gets whether or not this is a valid party, typically denoted by having more than one member.
     * This is here so implementation can do as pleased, and should be the only reliable
     * source. Checking for size won't always work, depending on implementation.
     *
     * @return whether the party is valid
     */
    val isParty: Boolean

    /**
     * Returns whether the provided player is in this party.
     *
     * @param player the player to check for
     * @return whether the provided player is in this party
     */
    fun isMember(player: EntityPlayer): Boolean

    operator fun contains(player: EntityPlayer) = isMember(player)

    /**
     * Invite someone to this party.
     *
     * @param player the player to invite
     */
    fun invite(player: EntityPlayer): Boolean

    /**
     * Cancel a party invite.
     *
     * @param player the player who's invite is to cancel
     * @return whether the cancel was successful
     */
    fun cancel(player: EntityPlayer): Boolean

    /**
     * Checks whether a player is invited to this party.
     *
     * @param player the player who's invited state is to check
     * @return whether the provided player is invited to this party
     */
    fun isInvited(player: EntityPlayer): Boolean

    /**
     * Clean up the invites (invite timeout) based on current world time.
     *
     * @param time the current world time
     */
    fun cleanupInvites(time: Long) // TODO: this could use some improvements. Not used for now

    /**
     * Gets whether the provided player is the leader of this party.
     *
     * @param player the player to check for leadership
     * @return whether the player is leader
     */
    fun isLeader(player: EntityPlayer) = leader == player

    /**
     * Gets the list of invited players.
     */
    val invited: Sequence<EntityPlayer>

    fun readNBT(nbt: NBTTagCompound)

    fun writeNBT(): NBTTagCompound
}
