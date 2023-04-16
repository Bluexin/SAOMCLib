package com.tencao.saomclib.party

import com.tencao.saomclib.events.firePartyCreate
import java.util.concurrent.CopyOnWriteArrayList

object PartyManager {

    val parties = CopyOnWriteArrayList<IParty>()

    /*
    fun getParties(): Iterator<IParty>{
        return parties.iterator()
    }*/

    fun addParty(party: IParty) {
        parties.add(party)
    }

    fun getPartyObject(player: PlayerInfo): IParty? {
        return parties.firstOrNull { player in it }
        /*
        val partyIterator = parties.iterator()
        while (partyIterator.hasNext()){
            val party = partyIterator.next()
            if (player in party)
                return party
        }
        return null*/
    }

    fun createParty(player: PlayerInfo): IParty {
        val party = firePartyCreate(player)
        parties += party
        return party
    }

    fun getOrCreateParty(player: PlayerInfo): IParty {
        return getPartyObject(player)
            ?: createParty(player)
    }

    fun getInvitedParty(player: PlayerInfo, leader: PlayerInfo): IParty? {
        return parties.firstOrNull { it.leaderInfo.equals(leader) && it.isInvited(player) }
        /*
        val partyIterator = parties.iterator()
        while (partyIterator.hasNext()){
            val party = partyIterator.next()
            if (party.isInvited(player))
                return party
        }
        return null*/
    }

    fun getInvitedParties(player: PlayerInfo): Set<IParty> {
        return parties.filter { it.isInvited(player) }.toSet()
        /*
        val invitedParties = mutableSetOf<IParty>()
        val partyIterator = parties.iterator()
        while (partyIterator.hasNext()){
            val party = partyIterator.next()
            if (party.isInvited(player))
                invitedParties.add(party)
        }
        return invitedParties*/
    }

    fun removeParty(party: IParty) {
        parties.remove(party)/*
        val partyIterator = parties.iterator()
        while (partyIterator.hasNext()){
            val currentParty = partyIterator.next()
            if (currentParty == party) {
                partyIterator.remove()
                return
            }
        }*/
    }

    fun clean() {
        parties.clear()
    }
}
