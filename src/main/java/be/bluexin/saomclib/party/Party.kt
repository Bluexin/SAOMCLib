package be.bluexin.saomclib.party

import be.bluexin.saomclib.capabilities.getPartyCapability
import net.minecraft.entity.player.EntityPlayer
import java.lang.ref.WeakReference
import java.util.*

/**
 * Part of saomclib by Bluexin.
 *
 * @author Bluexin
 */
class Party(leader: EntityPlayer) : IParty { // TODO: sync client

    override fun addMember(member: EntityPlayer): Boolean {
        if (!isMember(member)) {
            membersImpl.put(member, Unit)
            invitesImpl.remove(member)
            return true
        }

        return false
    }

    override fun removeMember(member: EntityPlayer) = if (membersImpl.remove(member) != null) {
        leader = null
        if (!isParty) dissolve()
        true
    } else false

    override val members: List<EntityPlayer>
        get() = membersImpl.keys.toList()

    override var leader: EntityPlayer?
        get() = leaderImpl?.get()
        set(value) {
            if (value == null) {
                if (isParty) leaderImpl = WeakReference(membersImpl.keys.first())
            } else if (isMember(value)) leaderImpl = WeakReference(value)
            else throw IllegalStateException("Target to be promoted isn't in the party!")
        }

    private var leaderImpl: WeakReference<EntityPlayer>? = null

    private val membersImpl = WeakHashMap<EntityPlayer, Unit>()

    private val invitesImpl = WeakHashMap<EntityPlayer, Long>()

    override fun dissolve() {
        members.forEach { it.getPartyCapability().clear() }
        membersImpl.clear()
    }

    override val size: Int
        get() = membersImpl.size

    override val isParty = size > 1

    override fun isMember(player: EntityPlayer) = membersImpl.contains(player)

    override fun invite(player: EntityPlayer): Boolean {
        if (!isMember(player) && !isInvited(player)) {
            invitesImpl.put(player, player.world.worldTime)
            return true
        }
        return false
    }

    override fun cancel(player: EntityPlayer) = invitesImpl.remove(player) != null

    override fun isInvited(player: EntityPlayer) = invitesImpl.contains(player)

    override fun cleanupInvites(time: Long) {
        val remove = invitesImpl.filter { (if (time < it.value) time + 24000 else time) - it.value > 300 }
        remove.forEach { invitesImpl.remove(it.key) }
    }

    init {
        membersImpl.put(leader, Unit)
        this.leader = leader
    }
}
