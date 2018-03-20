package be.bluexin.saomclib.events

import be.bluexin.saomclib.party.IParty
import net.minecraft.entity.player.EntityPlayer
import net.minecraftforge.fml.common.eventhandler.Event

/**
 * Party-related events.
 * They are fired both on client and server.
 */
abstract class PartyEvent(val party: IParty): Event() {
    /**
     * Fired when a player actually joins a party.
     */
    class Join(party: IParty, val player: EntityPlayer): PartyEvent(party)
    class Leave(party: IParty, val player: EntityPlayer): PartyEvent(party)
    class Disbanded(party: IParty): PartyEvent(party)
    @Deprecated("Should we really have a distinction with Leave? Unused for now.")
    class Kicked(party: IParty, val player: EntityPlayer): PartyEvent(party)
    class LeaderChanged(party: IParty, val player: EntityPlayer): PartyEvent(party)
    class Invited(party: IParty, val player: EntityPlayer): PartyEvent(party)
    class InviteCanceled(party: IParty, val player: EntityPlayer): PartyEvent(party)
}