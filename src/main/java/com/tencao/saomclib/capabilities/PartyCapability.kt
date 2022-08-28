package com.tencao.saomclib.capabilities

import com.tencao.saomclib.SAOMCLib
import com.tencao.saomclib.getOrNull
import com.tencao.saomclib.onClient
import com.tencao.saomclib.onServer
import com.tencao.saomclib.packets.PartyType
import com.tencao.saomclib.party.IPartyData
import com.tencao.saomclib.party.PartyClientObject
import com.tencao.saomclib.party.PartyManager
import com.tencao.saomclib.party.playerInfo
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.nbt.CompoundNBT
import net.minecraft.nbt.INBT
import net.minecraft.nbt.ListNBT
import net.minecraft.util.Direction
import net.minecraft.util.ResourceLocation
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.common.capabilities.CapabilityInject

class PartyCapability : AbstractEntityCapability() {

    var partyData: IPartyData? = null
        get() {
            return if (reference.get()?.world?.isRemote == false) {
                PartyManager.getPartyObject((reference.get() as PlayerEntity).playerInfo())
            } else field
        }
    var inviteData: MutableSet<IPartyData> = mutableSetOf()
        get() {
            return if (reference.get()?.world?.isRemote == false) {
                PartyManager.getInvitedParties((reference.get() as PlayerEntity).playerInfo()).toMutableSet()
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
        override fun readNBT(capability: Capability<PartyCapability>?, instance: PartyCapability, side: Direction?, nbt: INBT?) {
            val nbtTagCompound = nbt as? CompoundNBT ?: return
            if (instance.reference.get() is PlayerEntity) {
                val world = instance.reference.get()?.world
                world?.onServer {
                    instance.partyData = PartyManager.getPartyObject((instance.reference.get() as PlayerEntity).playerInfo())
                    instance.inviteData = PartyManager.getInvitedParties((instance.reference.get() as PlayerEntity).playerInfo()).toMutableSet()
                }
                world?.onClient {
                    if (nbtTagCompound.contains("party")) {
                        instance.partyData = PartyClientObject.readNBT(nbtTagCompound.getCompound("party"))
                    }
                    if (nbtTagCompound.contains("invitedList")) {
                        val tagList = nbtTagCompound.getList("invitedList", 10)
                        for (i in 0 until tagList.count()) {
                            instance.inviteData.add(PartyClientObject.readNBT(tagList.getCompound(i))!!)
                        }
                    }
                }
            }
        }

        override fun writeNBT(capability: Capability<PartyCapability>?, instance: PartyCapability, side: Direction?): INBT {
            val nbt = CompoundNBT()

            if (instance.partyData != null) {
                nbt.put("party", instance.partyData!!.writeNBT())
            }
            if (instance.inviteData.isNotEmpty()) {
                val tagList = ListNBT()
                instance.inviteData.forEach {
                    tagList.add(it.writeNBT())
                }
                nbt.put("invitedList", tagList)
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

fun PlayerEntity.getPartyCapability() = this.getCapability(PartyCapability.CAP_INSTANCE, null).resolve().getOrNull()
