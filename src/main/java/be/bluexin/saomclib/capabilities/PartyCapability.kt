package be.bluexin.saomclib.capabilities

import be.bluexin.saomclib.SAOMCLib
import be.bluexin.saomclib.party.IParty
import be.bluexin.saomclib.party.Party
import net.minecraft.entity.Entity
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.nbt.NBTBase
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.EnumFacing
import net.minecraft.util.ResourceLocation
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.common.capabilities.CapabilityInject
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
            invitedToImpl = if (value != null) WeakReference(value)
            else null
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

    override fun restore(entity: Entity, original: Entity): Boolean {
        val old = original.getCapability(CAP_INSTANCE, null)?: return true
        this.party = old.party
        this.invitedTo = old.invitedTo

        this.party?.fixPostDeath(original as EntityPlayer, entity as EntityPlayer)
        this.invitedTo?.fixPostDeath(original as EntityPlayer, entity as EntityPlayer)

        return false
    }

    override val shouldSyncOnDeath = true
    override val shouldSyncOnDimensionChange = true
    override val shouldRestoreOnDeath = true
    override val shouldSendOnLogin = true

    companion object {
        @Key val KEY = ResourceLocation(SAOMCLib.MODID, "party")

        @CapabilityInject(PartyCapability::class)
        lateinit var CAP_INSTANCE: Capability<PartyCapability>
    }

    object PartyStorage: Capability.IStorage<PartyCapability> {
        override fun readNBT(capability: Capability<PartyCapability>, instance: PartyCapability, side: EnumFacing?, nbt: NBTBase) {
            val nbtTagCompound = nbt as? NBTTagCompound?: return

            if (nbtTagCompound.hasKey("party")) {
                val pt = instance.getOrCreatePT()
                pt.readNBT(nbtTagCompound.getCompoundTag("party"))
            } else instance.party = null
            if (nbtTagCompound.hasKey("invited")) {
                val pt = Party(instance.reference.get() as EntityPlayer)
                pt.readNBT(nbtTagCompound.getCompoundTag("invited"))
                instance.invitedTo = pt
            } else instance.invitedTo = null
        }

        override fun writeNBT(capability: Capability<PartyCapability>, instance: PartyCapability, side: EnumFacing?): NBTBase {
            val nbt = NBTTagCompound()

            if (instance.party != null) nbt.setTag("party", instance.party!!.writeNBT())
            if (instance.invitedTo != null) nbt.setTag("invited", instance.invitedTo!!.writeNBT())

            return nbt
        }
    }
}

fun EntityPlayer.getPartyCapability() = this.getCapability(PartyCapability.CAP_INSTANCE, null)!!
