package be.bluexin.saomclib.party

import be.bluexin.saomclib.events.firePartyCreate
import net.minecraft.entity.player.EntityPlayer
import java.util.concurrent.CopyOnWriteArraySet

object PartyManager {

    val parties = CopyOnWriteArraySet<IParty>()

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

    fun getInvitedParties(player: EntityPlayer): Set<IParty>{
        return parties.filter { it.isInvited(player) }.toSet()
    }

    fun removeParty(party: IParty){
        parties.remove(party)
    }

    fun clean(){
        parties.clear()
    }

    fun getPartyIterator(): MutableIterator<IParty>{
        return parties.iterator()
    }

}