package com.tencao.saomclib.events

import com.tencao.saomclib.party.*
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.common.eventhandler.Cancelable
import net.minecraftforge.fml.common.eventhandler.Event

open class PartyEvent(val partyData: IPartyData) : Event() {

    /**
     * Fired when a player tries to join the party.
     * Only fired server side
     * @param party The party data in question
     * @param player The player who's attempting to join
     * @return If event is cancelled, the join is cancelled
     */
    @Cancelable
    class JoinCheck(party: IPartyData, val player: PlayerInfo) : PartyEvent(party)

    /**
     * Fired when a player joins the party.
     * Fired both on client and server
     * @param party The party data in question
     * @param player The player who joined
     */
    class Join(party: IPartyData, val player: PlayerInfo) : PartyEvent(party)

    /**
     * Fired when a player actually leaves the party.
     * Fired both on client and server
     * @param party The party data in question
     * @param player The player who left
     */
    class Leave(party: IPartyData, val player: PlayerInfo) : PartyEvent(party)

    /**
     * Fired when the party disbands
     * Fired both on client and server
     * @param party The party data in question
     */
    class Disbanded(party: IPartyData) : PartyEvent(party)

    /**
     * Fired when a player is about to be kicked from the party.
     * Only fired server side
     * @param party The party data in question
     * @param player The player who's about to be kicked
     * @return If event is cancelled, the kick is cancelled
     */
    @Cancelable
    class KickCheck(party: IPartyData, val player: PlayerInfo) : PartyEvent(party)

    /**
     * Fired when a player is kicked from the party.
     * Fired both on client and server
     * @param party The party data in question
     * @param player The player who's kicked
     */
    class Kicked(party: IPartyData, val player: PlayerInfo) : PartyEvent(party)

    /**
     * Fired when the leader has left and a new leader is needed.
     * Only fired server side
     * @param party The party data in question
     * @param player The player who will be the next leader, if
     * null, the first member on the members list will be the leader
     */
    class LeaderLeft(party: IPartyData, val player: PlayerInfo?) : PartyEvent(party)

    /**
     * Fired when the leader has changed.
     * Fired both on client and server
     * @param party The party data in question
     * @param newLeader The new leader
     * @param oldLeader The old leader
     */
    class LeaderChanged(party: IPartyData, val newLeader: PlayerInfo, val oldLeader: PlayerInfo) : PartyEvent(party)

    /**
     * Fired when a player is about to be invited.
     * Only fired server side
     * @param party The party data in question
     * @param player The player about to be invited
     * @return If event is cancelled, the invite is cancelled
     */
    @Cancelable
    class InviteCheck(party: IPartyData, val player: PlayerInfo) : PartyEvent(party)

    /**
     * Fired when a player is invited
     * Fired both on client and server
     * @param party The party data in question
     * @param player The player invited
     */
    class Invited(party: IPartyData, val player: PlayerInfo) : PartyEvent(party)

    /**
     * Fired when a player cancels their invite
     * Only fired after a player has been successfully invited
     * Fired both on client and server
     * @param party The party data in question
     * @param player The player invited
     */
    class InviteCanceled(party: IPartyData, val player: PlayerInfo) : PartyEvent(party)

    /**
     * Fired when there's been a change in the party
     * Useful for renderers or other hooks that depend
     * on the data being up to date.
     */
    class Refresh(party: IPartyData) : PartyEvent(party)
}
class PartyCreate(val party: IParty) : Event()

fun IPartyData.fireJoinCheck(player: PlayerInfo): Boolean {
    return !MinecraftForge.EVENT_BUS.post(PartyEvent.JoinCheck(this, player))
}

fun IPartyData.fireJoin(player: PlayerInfo) {
    MinecraftForge.EVENT_BUS.post(PartyEvent.Join(this, player))
}

fun IPartyData.fireLeave(player: PlayerInfo) {
    MinecraftForge.EVENT_BUS.post(PartyEvent.Leave(this, player))
}

fun IPartyData.fireDisbanded() {
    MinecraftForge.EVENT_BUS.post(PartyEvent.Disbanded(this))
}

fun IPartyData.fireKickCheck(player: PlayerInfo): Boolean {
    return !MinecraftForge.EVENT_BUS.post(PartyEvent.KickCheck(this, player))
}

fun IPartyData.fireKicked(player: PlayerInfo) {
    MinecraftForge.EVENT_BUS.post(PartyEvent.Kicked(this, player))
}

fun IPartyData.fireLeaderLeft(): PlayerInfo? {
    val event = PartyEvent.LeaderLeft(this, null)
    MinecraftForge.EVENT_BUS.post(event)
    return event.player
}

fun IPartyData.fireLeaderChanged(newLeader: PlayerInfo, oldLeader: PlayerInfo) {
    MinecraftForge.EVENT_BUS.post(PartyEvent.LeaderChanged(this, newLeader, oldLeader))
}

fun IPartyData.fireInviteCheck(player: PlayerInfo): Boolean {
    return !MinecraftForge.EVENT_BUS.post(PartyEvent.InviteCheck(this, player))
}

fun IPartyData.fireInvited(player: PlayerInfo) {
    MinecraftForge.EVENT_BUS.post(PartyEvent.Invited(this, player))
}

fun IPartyData.fireInviteCanceled(player: PlayerInfo) {
    MinecraftForge.EVENT_BUS.post(PartyEvent.InviteCanceled(this, player))
}

fun IPartyData.fireRefresh() {
    MinecraftForge.EVENT_BUS.post(PartyEvent.Refresh(this))
}

fun PartyManager.firePartyCreate(player: PlayerInfo): IParty {
    val event = PartyCreate(PartyServerObject(player))
    MinecraftForge.EVENT_BUS.post(event)
    return event.party
}
