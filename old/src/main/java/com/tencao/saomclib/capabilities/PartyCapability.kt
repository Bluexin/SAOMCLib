package com.tencao.saomclib.capabilities

import com.tencao.saomclib.SAOMCLib
import com.tencao.saomclib.onClient
import com.tencao.saomclib.onServer
import com.tencao.saomclib.packets.party.PartyType
import com.tencao.saomclib.party.IPartyData
import com.tencao.saomclib.party.PartyClientObject
import com.tencao.saomclib.party.PartyManager
import com.tencao.saomclib.party.playerInfo
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.nbt.NBTBase
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.nbt.NBTTagList
import net.minecraft.util.EnumFacing
import net.minecraft.util.ResourceLocation
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.common.capabilities.CapabilityInject

class PartyCapability : AbstractEntityCapability() {

    var partyData: IPartyData? = null
        get() {
            return if (reference.get()?.world?.isRemote == false) {
                PartyManager.getPartyObject((reference.get() as EntityPlayer).playerInfo())
            } else field
        }
    var inviteData: MutableSet<IPartyData> = mutableSetOf()
        get() {
            return if (reference.get()?.world?.isRemote == false) {
                PartyManager.getInvitedParties((reference.get() as EntityPlayer).playerInfo()).toMutableSet()
            } else field
        }

    fun setPartyData(partyData: IPartyData?, partyType: PartyType) {
        when (partyType) {
            PartyType.MAIN -> this.partyData = partyData
            PartyType.INVITE -> {
                if (partyData != null) {
                    this.inviteData.removeIf { it.isLeader(partyData.leaderInfo) }
                    this.inviteData.add(partyData)
                }
            }
        }
    }

    override val shouldSyncOnDeath = true
    override val shouldSyncOnDimensionChange = true
    override val shouldRestoreOnDeath = true
    override val shouldSendOnLogin = true

    object PartyStorage : Capability.IStorage<PartyCapability> {
        override fun readNBT(capability: Capability<PartyCapability>?, instance: PartyCapability, side: EnumFacing?, nbt: NBTBase?) {
            val nbtTagCompound = nbt as? NBTTagCompound ?: return
            if (instance.reference.get() is EntityPlayer) {
                val world = instance.reference.get()?.world
                world?.onServer {
                    instance.partyData = PartyManager.getPartyObject((instance.reference.get() as EntityPlayer).playerInfo())
                    instance.inviteData = PartyManager.getInvitedParties((instance.reference.get() as EntityPlayer).playerInfo()).toMutableSet()
                }
                world?.onClient {
                    if (nbtTagCompound.hasKey("party")) {
                        instance.partyData = PartyClientObject.readNBT(nbtTagCompound.getCompoundTag("party"))
                    }
                    if (nbtTagCompound.hasKey("invitedList")) {
                        val tagList = nbtTagCompound.getTagList("invitedList", 10)
                        tagList.tagCount()
                        for (i in 0 until tagList.tagCount()) {
                            instance.inviteData.add(PartyClientObject.readNBT(tagList.getCompoundTagAt(i))!!)
                        }
                    }
                }
            }
        }

        override fun writeNBT(capability: Capability<PartyCapability>?, instance: PartyCapability, side: EnumFacing?): NBTBase {
            val nbt = NBTTagCompound()

            if (instance.partyData != null) {
                nbt.setTag("party", instance.partyData!!.writeNBT())
            }
            if (instance.inviteData.isNotEmpty()) {
                val tagList = NBTTagList()
                instance.inviteData.forEach {
                    tagList.appendTag(it.writeNBT())
                }
                nbt.setTag("invitedList", tagList)
            }
            return nbt
        }
    }

    companion object {
        @Key
        val KEY = ResourceLocation(SAOMCLib.MODID, "party")

        @CapabilityInject(PartyCapability::class)
        lateinit var CAP_INSTANCE: Capability<PartyCapability>
    }
}

fun EntityPlayer.getPartyCapability() = this.getCapability(PartyCapability.CAP_INSTANCE, null)!!
