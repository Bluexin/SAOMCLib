package be.bluexin.saomclib.capabilities

import be.bluexin.saomclib.SAOMCLib
import be.bluexin.saomclib.party.IParty
import be.bluexin.saomclib.party.Party
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.ResourceLocation
import java.lang.ref.WeakReference

/**
 * Part of saomclib by Bluexin.
 *
 * @author Bluexin
 */
class PartyCapability : AbstractEntityCapability() {

    var party: IParty? = null
    private var invitedToImpl: WeakReference<IParty>? = null

    var invitedTo: IParty?
        get() = invitedToImpl?.get()
        set(value) {
            if (value != null) invitedToImpl = WeakReference(value)
            else invitedToImpl = null
        }

    fun getOrCreatePT(): IParty {
        if (party == null) party = Party(this.reference.get() as EntityPlayer)
        return party!!
    }

    fun clear() {
        party?.removeMember(this.reference.get() as EntityPlayer)
        party = null
        invitedTo = null
    }

    // TODO: properly do this
    override val shouldSyncOnDeath = false
    override val shouldSyncOnDimensionChange = false
    override val shouldRestoreOnDeath = true
    override val shouldSendOnLogin = false

    companion object {
        @Key val KEY = ResourceLocation(SAOMCLib.MODID, "party")
    }

    override fun loadNBTData(compound: NBTTagCompound?) {
    }

    override fun saveNBTData(compound: NBTTagCompound?) {
    }
}

fun EntityPlayer.getPartyCapability() = this.getExtendedProperties(PartyCapability.KEY.toString()) as PartyCapability
