package be.bluexin.saomclib.party

import it.unimi.dsi.fastutil.objects.Object2LongMap
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.nbt.NBTTagList
import java.util.*

interface IPartyData: Cloneable{
    /**
     * Gets the list of current leader.
     */
    var leaderInfo: PlayerInfo

    /**
     * Gets the list of current members.
     */
    val membersInfo: MutableCollection<PlayerInfo>

    /**
     * Gets the list of invited players.
     */
    val invitedInfo: Object2LongMap<PlayerInfo>


    operator fun contains(player: EntityPlayer) = isMember(player)
    operator fun contains(player: UUID) = isMember(player)
    operator fun contains(player: PlayerInfo) = isMember(player)

    /**
     * Gets the size of this party, aka the amount of members.
     *
     * @return the amount of members in this party
     */
    val size: Int
        get() = membersInfo.size

    /**
     * Gets whether or not this is a valid party, typically denoted by having more than one member.
     * This is here so implementation can do as pleased, and should be the only reliable
     * source. Checking for size won't always work, depending on implementation.
     *
     * @return whether the party is valid
     */
    val isParty: Boolean
        get() = membersInfo.any { it != leaderInfo } || (membersInfo.isNotEmpty() && invitedInfo.isNotEmpty())

    /**
     * Returns whether the provided player is in this party.
     *
     * @param player the player to check for
     * @return whether the provided player is in this party
     */
    fun isMember(player: EntityPlayer): Boolean = isMember(PlayerInfo(player))

    fun isMember(player: UUID): Boolean = isMember(PlayerInfo(player))

    fun isMember(player: PlayerInfo): Boolean = membersInfo.contains(player)

    /**
     * Checks whether a player is invited to this party.
     *
     * @param player the player who's invited state is to check
     * @return whether the provided player is invited to this party
     */
    fun isInvited(player: EntityPlayer): Boolean = isInvited(PlayerInfo(player))

    fun isInvited(player: UUID): Boolean = isInvited(PlayerInfo(player))

    fun isInvited(player: PlayerInfo): Boolean = invitedInfo.containsKey(player)

    /**
     * Gets whether the provided player is the leader of this party.
     *
     * @param player the player to check for leadership
     * @return whether the player is leader
     */
    fun isLeader(player: EntityPlayer) = leaderInfo.equals(player)

    fun isLeader(player: UUID) = leaderInfo.equals(player)

    fun isLeader(player: PlayerInfo) = leaderInfo == player

    /*
    fun readNBT(nbt: NBTTagCompound) {
        leaderInfo = PlayerInfo(UUID.fromString(nbt.getString("leader")))
        val membersTag = nbt.getTagList("members", NBTTagString("").id.toInt())
        val invitesTag = nbt.getTagList("invites", NBTTagCompound().id.toInt())
        membersInfo.clear()
        membersTag.forEach {
            val uuid = (it as NBTTagString).string
            val player = PlayerInfo(UUID.fromString(uuid))
            membersInfo += player
        }
        invitedInfo.clear()
        invitesTag.forEach {
            it as NBTTagCompound
            @Suppress("ReplacePutWithAssignment") // would introduce boxing
            invitedInfo.put(PlayerInfo(
                    UUID.fromString(it.getString("uuid"))),
                    it.getLong("time")
            )
        }
    }*/

    fun writeNBT(): NBTTagCompound {
        var tags: NBTTagCompound
        val membersTag = NBTTagList()
        membersInfo.asIterable().forEach {
            tags = NBTTagCompound()
            tags.setString("uuid", it.uuidString)
            tags.setString("name", it.username)
            membersTag.appendTag(tags)
        }
        val invitesTag = NBTTagList()
        invitedInfo.asIterable().forEach {
            tags = NBTTagCompound()
            tags.setString("uuid", it.key.uuidString)
            tags.setString("name", it.key.username)
            tags.setLong("time", it.value)
            invitesTag.appendTag(tags)
        }

        val nbt = NBTTagCompound()
        nbt.setString("leader", leaderInfo.uuidString)
        nbt.setTag("members", membersTag)
        nbt.setTag("invites", invitesTag)

        return nbt
    }

    public override fun clone(): Any {
        return super.clone()
    }

}