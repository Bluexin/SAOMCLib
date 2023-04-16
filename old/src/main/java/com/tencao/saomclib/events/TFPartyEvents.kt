package com.tencao.saomclib.events

import com.buuz135.togetherforever.api.IPlayerInformation
import com.buuz135.togetherforever.api.ITogetherTeam
import com.buuz135.togetherforever.api.TogetherForeverAPI
import com.buuz135.togetherforever.api.data.DefaultPlayerInformation
import com.buuz135.togetherforever.api.data.DefaultTogetherTeam
import com.buuz135.togetherforever.api.event.TeamEvent
import com.tencao.saomclib.party.IParty
import com.tencao.saomclib.party.IPartyData
import com.tencao.saomclib.party.PartyManager
import com.tencao.saomclib.party.PlayerInfo
import net.minecraftforge.fml.common.eventhandler.EventPriority
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object TFPartyEvents {

    /*******************************
     * TOGETHER FOREVER PARTY EVENTS
     *******************************/
    @SubscribeEvent(priority = EventPriority.LOWEST)
    fun onPartyJoin(e: TeamEvent.PlayerAdd) {
        PartyManager.getPartyObject(PlayerInfo(e.togetherTeam.owner))?.addMember(e.playerInformation.uuid) ?: run {
            val party = createParty(e.togetherTeam)
            party.addMember(e.playerInformation.uuid)
            PartyManager.addParty(party)
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    fun onPartyLeave(e: TeamEvent.RemovePlayer) {
        PartyManager.getPartyObject(PlayerInfo(e.togetherTeam.owner))?.removeMember(e.playerInformation.uuid) ?: run {
            val party = createParty(e.togetherTeam)
            party.removeMember(e.playerInformation.uuid)
            PartyManager.addParty(party)
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    fun onPartyCreate(e: TeamEvent.Create) {
        if (PartyManager.getPartyObject(PlayerInfo(e.togetherTeam.owner)) == null) {
            createParty(e.togetherTeam)
        }
    }

    /*******************************
     *    SAOMCLib PARTY EVENTS
     *******************************/
    @SubscribeEvent
    fun onPartyAdd(e: PartyEvent.Join) {
        val team: ITogetherTeam = getParty(e.partyData)
        team.addPlayer(createPlayerInfo(e.player))
        markDirty()
    }

    @SubscribeEvent
    fun onPartyLeave(e: PartyEvent.Leave) {
        val team: ITogetherTeam = getParty(e.partyData)
        team.removePlayer(createPlayerInfo(e.player))

        // If last player remains, remove party.
        if (team.players.size == 1 && TogetherForeverAPI.getInstance().teamInvites.none { it.sender == team.players.first() }) {
            team.removePlayer(team.players.first())
        }
        markDirty()
    }

    @SubscribeEvent
    fun onPartyInvite(e: PartyEvent.Invited) {
        TogetherForeverAPI.getInstance().createTeamInvite(createPlayerInfo(e.partyData.leaderInfo), createPlayerInfo(e.player), false)
    }

    @SubscribeEvent
    fun onPartyInviteCancel(e: PartyEvent.InviteCanceled) {
        TogetherForeverAPI.getInstance().teamInvites.removeIf { it.sender.uuid == e.partyData.leaderInfo.uuid && it.reciever.uuid == e.player.uuid }
    }

    @SubscribeEvent
    fun onLeaderChange(e: PartyEvent.LeaderChanged) {
        // If old party exists, remove
        removeParty(e.oldLeader)
        // Create new instance of party with new leader
        createParty(e.partyData)
    }

    @SubscribeEvent
    fun onDisbandParty(e: PartyEvent.Disbanded) {
        removeParty(e.partyData.leaderInfo)
    }

    /*******************************
     *           UTILITY
     *******************************/
    private fun markDirty() {
        TogetherForeverAPI.getInstance().getDataManager(TogetherForeverAPI.getInstance().world)?.markDirty()
    }

    private fun getParty(party: IPartyData): ITogetherTeam {
        return TogetherForeverAPI.getInstance().getPlayerTeam(party.leaderInfo.uuid) ?: createParty(party)
    }

    private fun removeParty(playerInfo: PlayerInfo) {
        TogetherForeverAPI.getInstance().getPlayerTeam(playerInfo.uuid)?.let { party ->
            TogetherForeverAPI.getInstance().getDataManager(TogetherForeverAPI.getInstance().world)?.let { data ->
                data.teams.remove(party)
                data.markDirty()
            }
        }
    }

    private fun createParty(e: IPartyData): ITogetherTeam {
        val party = DefaultTogetherTeam()
        party.addPlayer(createPlayerInfo(e.leaderInfo))
        val members = e.membersInfo.iterator()
        while (members.hasNext()) {
            val member = members.next()
            party.addPlayer(createPlayerInfo(member))
        }
        TogetherForeverAPI.getInstance().addTeam(party)
        return party
    }

    private fun createParty(e: ITogetherTeam): IParty {
        val party = PartyManager.createParty(PlayerInfo(e.owner))
        e.players.forEach { party.addMember(it.uuid) }
        return party
    }

    private fun createPlayerInfo(playerInfo: PlayerInfo): IPlayerInformation {
        val info = DefaultPlayerInformation()
        info.name = playerInfo.username
        info.uuid = playerInfo.uuid
        return info
    }
}
