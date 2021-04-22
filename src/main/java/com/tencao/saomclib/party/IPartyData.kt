package com.tencao.saomclib.party

import com.tencao.saomclib.SAOMCLib
import com.tencao.saomclib.proxy.IProxy
import it.unimi.dsi.fastutil.objects.Object2LongMap
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.nbt.CompoundNBT
import net.minecraft.nbt.ListNBT
import java.util.*

abstract class IPartyData: Cloneable{
    /**
     * Gets the list of current leader.
     */
    abstract var leaderInfo: PlayerInfo
        internal set

    /**
     * Gets the list of current members.
     */
    internal abstract val membersInfo: HashSet<PlayerInfo>

    /**
     * Gets the list of invited players.
     */
    internal abstract val invitedInfo: Object2LongMap<PlayerInfo>

    fun getMembers(): List<PlayerInfo>{
        val memberList = arrayListOf<PlayerInfo>()
        val members = membersInfo.iterator()
        while (members.hasNext()){
            memberList.add(members.next())
        }
        return memberList
    }

    fun getInvited(): List<PlayerInfo>{
        val invitedList = arrayListOf<PlayerInfo>()
        val members = invitedInfo.iterator()
        while (members.hasNext()){
            invitedList.add(members.next().key)
        }
        return invitedList
    }

    operator fun contains(player: PlayerEntity) = isMember(player)
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
        get() = membersInfo.count() > 1 || (membersInfo.isNotEmpty() && invitedInfo.isNotEmpty())

    /**
     * Returns whether the provided player is in this party.
     *
     * @param player the player to check for
     * @return whether the provided player is in this party
     */
    fun isMember(player: PlayerEntity): Boolean = isMember(PlayerInfo(player))

    fun isMember(player: UUID): Boolean = isMember(PlayerInfo(player))

    fun isMember(player: PlayerInfo): Boolean {
        val members = membersInfo.iterator()
        while (members.hasNext()){
            val member = members.next()
            if (member == player)
                return true
        }
        return false
    }

    /**
     * Checks whether a player is invited to this party.
     *
     * @param player the player who's invited state is to check
     * @return whether the provided player is invited to this party
     */
    fun isInvited(player: PlayerEntity): Boolean = isInvited(PlayerInfo(player))

    fun isInvited(player: UUID): Boolean = isInvited(PlayerInfo(player))

    fun isInvited(player: PlayerInfo): Boolean {
        val members = invitedInfo.iterator()
        while (members.hasNext()){
            val member = members.next()
            if (member.key == player)
                return true
        }
        return false
    }

    /**
     * Gets whether the provided player is the leader of this party.
     *
     * @param player the player to check for leadership
     * @return whether the player is leader
     */
    fun isLeader(player: PlayerEntity) = leaderInfo.equals(player)

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

    fun writeNBT(): CompoundNBT {
        var tags: CompoundNBT
        val membersTag = ListNBT()
        membersInfo.asIterable().forEach {
            tags = CompoundNBT()
            tags.putString("uuid", it.uuidString)
            tags.putString("name", it.username)
            membersTag.add(tags)
        }
        val invitesTag = ListNBT()
        invitedInfo.asIterable().forEach {
            tags = CompoundNBT()
            tags.putString("uuid", it.key.uuidString)
            tags.putString("name", it.key.username)
            tags.putLong("time", it.value)
            invitesTag.add(tags)
        }

        val nbt = CompoundNBT()
        nbt.putString("leader", leaderInfo.uuidString)
        nbt.put("members", membersTag)
        nbt.put("invites", invitesTag)

        return nbt
    }

    public override fun clone(): Any {
        return when (SAOMCLib.proxy.getSide){
            IProxy.ProxySide.CLIENT -> PartyClientObject(this)
            IProxy.ProxySide.SERVER -> PartyServerObject(this)
        }
    }

}