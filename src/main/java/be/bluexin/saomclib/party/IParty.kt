package be.bluexin.saomclib.party

import be.bluexin.saomclib.events.PartyEventV2
import it.unimi.dsi.fastutil.objects.Object2LongMap
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
     * Should fire [PartyEventV2.Join] when successful, both on client and server.
     *
     * @param member the member to add
     * @return whether the operation was successful (typically whether the player was already present, or following hypothetical size limit)
     */
    fun addMember(member: EntityPlayer): Boolean = addMember(PlayerInfo(member))

    fun addMember(member: IPlayerInfo): Boolean

    /**
     * Accepts a party invite.
     *
     * @param player the player accepting the invite
     * @return whether the operation was successful
     */
    fun acceptInvite(player: EntityPlayer): Boolean = acceptInvite(PlayerInfo(player))

    fun acceptInvite(player: IPlayerInfo): Boolean

    /**
     * Removes a member from this party.
     *
     * Should fire [PartyEventV2.Leave] when successful, both on client and server.
     *
     * @param member the member to remove
     * @return whether the operation was successful
     */
    fun removeMember(member: EntityPlayer): Boolean = removeMember(PlayerInfo(member))

    fun removeMember(member: IPlayerInfo): Boolean

    /**
     * Gets a sequence containing all the members of this party.
     * Returned sequence safety (concurrency, mutability, ...) is up to implementation details.
     *
     * @return all the members in this party
     * @Deprecated see [membersInfo]
     */
    @Deprecated(
            "Use #membersInfo as replacement (changes semantics)",
            level = DeprecationLevel.WARNING,
            replaceWith = ReplaceWith("this.membersInfo.mapNotNull(IPlayerInfo::player)")
    )
    val members: Sequence<EntityPlayer>
        get() = membersInfo.mapNotNull(IPlayerInfo::player)

    val membersInfo: Sequence<IPlayerInfo>

    /**
     * Gets the leader of this party.
     * Returned reference safety (concurrency, mutability, ...) is up to implementation details.
     *
     * Should fire [PartyEventV2.LeaderChanged] when set, both on client and server.
     *
     * @return the leader of this party.
     * @Deprecated see [leaderInfo]
     */
    @Deprecated(
            "Use #leaderInfo as replacement (changes semantics)",
            level = DeprecationLevel.WARNING,
            replaceWith = ReplaceWith("this.leaderInfo?.player")
    )
    var leader: EntityPlayer?
        get() = leaderInfo?.player
        set(value) {
            leaderInfo = if (value != null) PlayerInfo(value) else null
        }

    var leaderInfo: IPlayerInfo?

    /**
     * Dissolves this party, aka removing all the members.
     *
     * Should fire [PartyEventV2.Disbanded] when successful, both on client and server.
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
    fun isMember(player: EntityPlayer): Boolean = isMember(PlayerInfo(player))

    fun isMember(player: IPlayerInfo): Boolean

    operator fun contains(player: EntityPlayer) = isMember(player)
    operator fun contains(player: IPlayerInfo) = isMember(player)

    /**
     * Invite someone to this party.
     *
     * Should fire [PartyEventV2.Invited] when successful, both on client and server.
     *
     * @param player the player to invite
     */
    fun invite(player: EntityPlayer): Boolean = invite(PlayerInfo(player))

    fun invite(player: IPlayerInfo): Boolean

    /**
     * Cancel a party invite.
     *
     * Should fire [PartyEventV2.InviteCanceled] when successful, both on client and server.
     *
     * @param player the player who's invite is to cancel
     * @return whether the cancel was successful
     */
    fun cancel(player: EntityPlayer): Boolean = cancel(PlayerInfo(player))

    fun cancel(player: IPlayerInfo): Boolean

    /**
     * Checks whether a player is invited to this party.
     *
     * @param player the player who's invited state is to check
     * @return whether the provided player is invited to this party
     */
    fun isInvited(player: EntityPlayer): Boolean = isInvited(PlayerInfo(player))

    fun isInvited(player: IPlayerInfo): Boolean

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
    fun isLeader(player: EntityPlayer) = leaderInfo?.player == player

    fun isLeader(player: IPlayerInfo) = leaderInfo == player

    /**
     * Gets the list of invited players.
     * @Deprecated see [invitedInfo]
     */
    @Deprecated(
            "Use #invitedInfo as replacement (changes semantics)",
            level = DeprecationLevel.WARNING,
            replaceWith = ReplaceWith("this.invitedInfo.mapNotNull { it.key.player }")
    )
    val invited: Sequence<EntityPlayer>
        get() = invitedInfo.mapNotNull { it.key.player }

    /**
     * Gets the list of invited players.
     */
    val invitedInfo: Sequence<Object2LongMap.Entry<IPlayerInfo>>

    fun readNBT(nbt: NBTTagCompound)

    fun writeNBT(): NBTTagCompound

    /**
     * Fix a party after a player's death.
     * For the unaware, the EntityPlayer instance is different after resurrection.
     */
    @Suppress("DeprecatedCallableAddReplaceWith")
    @Deprecated("Shouldn't be needed anymore with #tryLoadPlayer", level = DeprecationLevel.ERROR)
    fun fixPostDeath(oldPlayer: EntityPlayer, newPlayer: EntityPlayer) = Unit

    /**
     * Try to load a newly created player.
     * Used to fix GC'd players, deaths, ...
     */
    fun tryLoadPlayer(player: EntityPlayer) = membersInfo.any { it.tryLoad(player) }
}
