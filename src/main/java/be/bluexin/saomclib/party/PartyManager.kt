package be.bluexin.saomclib.party

import be.bluexin.saomclib.events.firePartyCreate
import net.minecraft.entity.player.EntityPlayer

object PartyManager {

    private val parties = HashSet<IParty>()

    // Using mutable iterator for safe removals
    val partyList
        get() = parties.iterator()

    fun addParty(party: IParty){
        parties.add(party)
    }

    fun getPartyData(player: PlayerInfo): IPartyData?{
        return parties.firstOrNull { player in it }
    }

    fun getPartyData(player: EntityPlayer): IPartyData?{
        return parties.firstOrNull { player in it }
    }

    fun getPartyObject(player: PlayerInfo): IParty?{
        return parties.firstOrNull { player in it }
    }

    fun getPartyObject(player: EntityPlayer): IParty?{
        return parties.firstOrNull { player in it }
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

    fun getInvitedParty(player: EntityPlayer): IParty?{
        return parties.firstOrNull { it.isInvited(player) }
    }

    fun removeParty(party: IParty){
        val parties = partyList
        while (parties.hasNext()){
            if (parties.next() == party)
                parties.remove()
        }
    }

    fun cleanInvalidParties(){
        val parties = partyList
        while (parties.hasNext()){
            val party = parties.next()
            if (!party.isParty)
                parties.remove()
        }
    }

    fun clean(){
        parties.clear()
    }

}