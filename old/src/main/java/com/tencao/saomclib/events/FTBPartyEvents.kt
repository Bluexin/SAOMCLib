package com.tencao.saomclib.events

object FTBPartyEvents { /*

    /*******************************
     * FTBLIB PARTY EVENTS
     *******************************/


    @SubscribeEvent(priority = EventPriority.LOWEST)
    fun onPartyJoin(e: ForgeTeamPlayerJoinedEvent){

        PartyManager.getPartyObject(PlayerInfo(e.team.owner.player))?.addMember(e.player.id)?: run {
            val party = createParty(e.team)
            party.addMember(e.player.id)
            PartyManager.addParty(party)
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    fun onPartyLeave(e: ForgeTeamPlayerLeftEvent){
        PartyManager.getPartyObject(PlayerInfo(e.team.owner.player))?.removeMember(e.player.id)?: run {
            val party = createParty(e.team)
            party.removeMember(e.player.id)
            PartyManager.addParty(party)
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    fun onPartyCreate(e: ForgeTeamCreatedEvent){
        if (PartyManager.getPartyObject(PlayerInfo(e.team.owner.player)) == null)
            createParty(e.team)
    }

    /*******************************
     *    SAOMCLib PARTY EVENTS
     *******************************/
    @SubscribeEvent
    fun onPartyAdd(e: PartyEvent.Join){
        val team: ForgeTeam = Universe.get().getPlayer(e.partyData.leaderInfo.uuid)?.team?: createParty(e.partyData)
        team.addMember(getForgePlayer(e.player))
        team.addPlayer(TFPartyEvents.createPlayerInfo(e.player))
        TFPartyEvents.markDirty()
    }

    @SubscribeEvent
    fun onPartyLeave(e: PartyEvent.Leave){
        val team: ITogetherTeam = getParty(e.partyData)
        team.removePlayer(createPlayerInfo(e.player))

        // If last player remains, remove party.
        if (team.players.size == 1 && TogetherForeverAPI.getInstance().teamInvites.none { it.sender == team.players.first() })
            team.removePlayer(team.players.first())
        markDirty()
    }

    @SubscribeEvent
    fun onPartyInvite(e: PartyEvent.Invited){
        TogetherForeverAPI.getInstance().createTeamInvite(
            createPlayerInfo(e.partyData.leaderInfo),
            createPlayerInfo(e.player), false)
    }

    @SubscribeEvent
    fun onPartyInviteCancel(e: PartyEvent.InviteCanceled){
        TogetherForeverAPI.getInstance().teamInvites.removeIf { it.sender.uuid == e.partyData.leaderInfo.uuid && it.reciever.uuid == e.player.uuid }
    }

    @SubscribeEvent
    fun onLeaderChange(e: PartyEvent.LeaderChanged){
        //If old party exists, remove
        removeParty(e.oldLeader)
        //Create new instance of party with new leader
        createParty(e.partyData)
    }

    @SubscribeEvent
    fun onDisbandParty(e: PartyEvent.Disbanded){
        removeParty(e.partyData.leaderInfo)
    }

    /*******************************
     *           UTILITY
     *******************************/

    private fun getParty(party: IPartyData): ForgeTeam? {
        val forgePlayer = getForgePlayer(party.leaderInfo)
        return forgePlayer?.team?: createParty(party)
    }

    private fun createParty(e: IPartyData): ForgeTeam?{
        val p = getForgePlayer(e.leaderInfo)
        if (p != null) {
            val party =
                ForgeTeam(p.team.universe, p.team.universe.generateTeamUID(0), e.leaderInfo.username, TeamType.PLAYER)
            p.team = party
            party.owner = p
            e.membersInfo.forEach {
                val player = getForgePlayer(it)
                if (player != null)
                    party.addMember(player, false)
            }
            party.universe.addTeam(party)
            ForgeTeamCreatedEvent(party).post()
            return party
        }
        return null
    }

    private fun createParty(e: ForgeTeam): IParty {
        val party = PartyManager.createParty(PlayerInfo(e.owner.player))
        e.members.forEach { party.addMember(it.id) }
        return party
    }

    private fun getForgePlayer(e: PlayerInfo): ForgePlayer? {
        return Universe.get().getPlayer(e.uuid)
    }*/
}
