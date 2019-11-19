package be.bluexin.saomclib.party

import be.bluexin.saomclib.events.firePartyCreate
import net.minecraft.entity.player.EntityPlayer

object PartyManager {

    private val parties = HashSet<IParty>()
    val partyList = parties.asIterable()

    fun getPartyData(player: EntityPlayer): IPartyData?{
        return partyList.firstOrNull { player in it }
    }

    fun getPartyObject(player: EntityPlayer): IParty?{
        return partyList.firstOrNull { player in it }
    }

    fun createParty(player: EntityPlayer): IParty {
        val party = firePartyCreate(player)
        parties += party
        return party
    }

    fun getOrCreateParty(player: EntityPlayer): IParty {
        return getPartyObject(player)
                ?: createParty(player)
    }

    fun getInvitedParty(player: EntityPlayer): IParty?{
        return partyList.firstOrNull { it.isInvited(player) }
    }

    fun removeParty(party: IParty){
        parties.remove(party)
    }

    fun cleanInvalidParties(){
        parties.removeIf { !it.isParty }
    }

    fun clean(){
        parties.clear()
    }

}