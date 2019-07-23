@file:Suppress("DEPRECATION")

package be.bluexin.saomclib.events

import be.bluexin.saomclib.party.IParty
import be.bluexin.saomclib.party.IPlayerInfo
import net.minecraft.entity.player.EntityPlayer
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.common.eventhandler.Event

/**
 * Party-related events.
 * They are fired both on client and server.
 */
@Deprecated("Deprecated in favor of PartyEventV2", replaceWith = ReplaceWith("PartyEventV2"))
/*abstract*/sealed class PartyEvent(val party: IParty?) : Event() {
    /**
     * Fired when a player actually joins a party.
     */
    class Join(party: IParty, val player: EntityPlayer) : PartyEvent(party)

    class Leave(party: IParty, val player: EntityPlayer) : PartyEvent(party)
    class Disbanded(party: IParty) : PartyEvent(party)
    @Deprecated("Should we really have a distinction with Leave? Unused for now.")
    class Kicked(party: IParty, val player: EntityPlayer) : PartyEvent(party)

    class LeaderChanged(party: IParty, val player: EntityPlayer) : PartyEvent(party)
    class Invited(party: IParty, val player: EntityPlayer) : PartyEvent(party)
    class InviteCanceled(party: IParty, val player: EntityPlayer) : PartyEvent(party)
    class Refreshed(party: IParty?, val invited: IParty?) : PartyEvent(party) // TODO: this is somewhat temporary, until I figure out a nice way of doing this
}

/**
 * Party-related events.
 * They are fired both on client and server.
 * Don't fire these directly, use the fire methods below instead.
 */
/*abstract*/sealed class PartyEventV2(val party: IParty?) : Event() {
    /**
     * Fired when a player actually joins a party.
     */
    class Join(party: IParty, val player: IPlayerInfo) : PartyEventV2(party)

    class Leave(party: IParty, val player: IPlayerInfo) : PartyEventV2(party)
    class Disbanded(party: IParty) : PartyEventV2(party)
    @Deprecated("Should we really have a distinction with Leave? Unused for now.")
    class Kicked(party: IParty, val player: IPlayerInfo) : PartyEventV2(party)

    class LeaderChanged(party: IParty, val player: IPlayerInfo) : PartyEventV2(party)
    class Invited(party: IParty, val player: IPlayerInfo) : PartyEventV2(party)
    class InviteCanceled(party: IParty, val player: IPlayerInfo) : PartyEventV2(party)
    class Refreshed(party: IParty?, val invited: IParty?) : PartyEventV2(party) // TODO: this is somewhat temporary, until I figure out a nice way of doing this
}

fun IParty.fireJoin(player: IPlayerInfo): Boolean {
    val e1 = MinecraftForge.EVENT_BUS.post(PartyEventV2.Join(this, player))
    val aPlayer = player.player ?: return e1
    val e2 = MinecraftForge.EVENT_BUS.post(PartyEvent.Join(this, aPlayer))
    return e1 && e2
}

fun IParty.fireLeave(player: IPlayerInfo): Boolean {
    val e1 = MinecraftForge.EVENT_BUS.post(PartyEventV2.Leave(this, player))
    val aPlayer = player.player ?: return e1
    val e2 = MinecraftForge.EVENT_BUS.post(PartyEvent.Leave(this, aPlayer))
    return e1 && e2
}

fun IParty.fireDisbanded(): Boolean {
    val e1 = MinecraftForge.EVENT_BUS.post(PartyEventV2.Disbanded(this))
    val e2 = MinecraftForge.EVENT_BUS.post(PartyEvent.Disbanded(this))
    return e1 && e2
}

@Deprecated("Should we really have a distinction with Leave? Unused for now.")
fun IParty.fireKicked(player: IPlayerInfo): Boolean {
    val e1 = MinecraftForge.EVENT_BUS.post(PartyEventV2.Kicked(this, player))
    val aPlayer = player.player ?: return e1
    val e2 = MinecraftForge.EVENT_BUS.post(PartyEvent.Kicked(this, aPlayer))
    return e1 && e2
}

fun IParty.fireLeaderChanged(player: IPlayerInfo): Boolean {
    val e1 = MinecraftForge.EVENT_BUS.post(PartyEventV2.LeaderChanged(this, player))
    val aPlayer = player.player ?: return e1
    val e2 = MinecraftForge.EVENT_BUS.post(PartyEvent.LeaderChanged(this, aPlayer))
    return e1 && e2
}

fun IParty.fireInvited(player: IPlayerInfo): Boolean {
    val e1 = MinecraftForge.EVENT_BUS.post(PartyEventV2.Invited(this, player))
    val aPlayer = player.player ?: return e1
    val e2 = MinecraftForge.EVENT_BUS.post(PartyEvent.Invited(this, aPlayer))
    return e1 && e2
}

fun IParty.fireInviteCanceled(player: IPlayerInfo): Boolean {
    val e1 = MinecraftForge.EVENT_BUS.post(PartyEventV2.InviteCanceled(this, player))
    val aPlayer = player.player ?: return e1
    val e2 = MinecraftForge.EVENT_BUS.post(PartyEvent.InviteCanceled(this, aPlayer))
    return e1 && e2
}

fun IParty?.fireRefreshed(invited: IParty?): Boolean {
    val e1 = MinecraftForge.EVENT_BUS.post(PartyEventV2.Refreshed(this, invited))
    val e2 = MinecraftForge.EVENT_BUS.post(PartyEvent.Refreshed(this, invited))
    return e1 && e2
}
