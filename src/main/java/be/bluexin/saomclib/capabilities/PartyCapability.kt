package be.bluexin.saomclib.capabilities

import be.bluexin.saomclib.SAOMCLib
import be.bluexin.saomclib.events.fireInviteCanceled
import be.bluexin.saomclib.events.fireInvited
import be.bluexin.saomclib.onServer
import be.bluexin.saomclib.party.IParty
import be.bluexin.saomclib.party.IPlayerInfo
import be.bluexin.saomclib.party.Party
import be.bluexin.saomclib.party.PlayerInfo
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

    private val playerInfo: IPlayerInfo get() = PlayerInfo(reference.get() as EntityPlayer)

    var invitedTo: IParty? = null
        get() = field ?: invitedToImpl?.get()
        set(value) {
            val oldValue = invitedTo
            if (oldValue != value) {
                // order in ifs is important !
                if (oldValue != null && !oldValue.fireInviteCanceled(playerInfo) && value == null) field = null
                if (value != null && !value.fireInvited(playerInfo)) field = value
                reference.get()?.world?.onServer {
                    invitedToImpl = if (value != null) WeakReference(value) else null
                }
            }
        }

    fun getOrCreatePT(): IParty {
        if (party == null) party = Party(playerInfo)
        return party!!
    }

    fun clear() {
        val player = playerInfo
        println("Clearing ${player.username}'s party cap.")
        party?.removeMember(player)
        party = null
        invitedTo?.cancel(player)
        invitedTo = null
    }

    override fun restore(entity: Entity, original: Entity): Boolean {
        val old = original.getCapability(CAP_INSTANCE, null) ?: return true
        this.party = old.party
        this.invitedTo = old.invitedTo

        this.party?.tryLoadPlayer(entity as EntityPlayer)
        this.invitedTo?.tryLoadPlayer(entity as EntityPlayer)

        this.sync()

        return false
    }

    override val shouldSyncOnDeath = true
    override val shouldSyncOnDimensionChange = true
    override val shouldRestoreOnDeath = true
    override val shouldSendOnLogin = true

    companion object {
        @Key
        val KEY = ResourceLocation(SAOMCLib.MODID, "party")

        @CapabilityInject(PartyCapability::class)
        lateinit var CAP_INSTANCE: Capability<PartyCapability>
    }

    object PartyStorage : Capability.IStorage<PartyCapability> {
        override fun readNBT(capability: Capability<PartyCapability>, instance: PartyCapability, side: EnumFacing?, nbt: NBTBase) {
            val nbtTagCompound = nbt as? NBTTagCompound ?: return

            if (nbtTagCompound.hasKey("party")) {
                val pt = instance.getOrCreatePT()
                pt.readNBT(nbtTagCompound.getCompoundTag("party"))
            } else instance.party = null
            if (nbtTagCompound.hasKey("invited")) {
                val pt = Party(instance.playerInfo)
                pt.readNBT(nbtTagCompound.getCompoundTag("invited"))
                instance.invitedTo = pt
            } else {
                instance.invitedTo = null
            }
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
