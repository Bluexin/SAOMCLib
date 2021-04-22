package com.tencao.saomclib.party

import it.unimi.dsi.fastutil.objects.Object2LongLinkedOpenHashMap
import it.unimi.dsi.fastutil.objects.Object2LongMap
import net.minecraft.nbt.CompoundNBT
import net.minecraftforge.api.distmarker.Dist
import net.minecraftforge.api.distmarker.OnlyIn
import java.util.*

@OnlyIn(Dist.CLIENT)
data class PartyClientObject(override var leaderInfo: PlayerInfo,
                             override val membersInfo: HashSet<PlayerInfo> = hashSetOf(),
                             override val invitedInfo: Object2LongMap<PlayerInfo> = Object2LongLinkedOpenHashMap<PlayerInfo>().apply {
                                 defaultReturnValue(Long.MIN_VALUE)
                             }): IPartyData() {

    constructor(partyData: IPartyData): this(partyData.leaderInfo, partyData.membersInfo, partyData.invitedInfo)

    companion object{
        fun readNBT(nbtTagCompound: CompoundNBT?): PartyClientObject? {
            if (nbtTagCompound == null || nbtTagCompound.isEmpty) return null

            val leaderInfo = PlayerInfo(UUID.fromString(nbtTagCompound.getString("leader")))
            val partyObject = PartyClientObject(leaderInfo)
            val membersTag = nbtTagCompound.getList("members", CompoundNBT().id.toInt())
            val invitesTag = nbtTagCompound.getList("invites", CompoundNBT().id.toInt())
            partyObject.membersInfo.clear()
            membersTag.forEach {
                it as CompoundNBT
                partyObject.membersInfo += PlayerInfo(
                        UUID.fromString(it.getString("uuid")),
                        it.getString("name")
                )
            }
            partyObject.invitedInfo.clear()
            invitesTag.forEach {
                it as CompoundNBT
                @Suppress("ReplacePutWithAssignment") // would introduce boxing
                partyObject.invitedInfo.put(
                    PlayerInfo(
                        UUID.fromString(it.getString("uuid")),
                        it.getString("name")),
                        it.getLong("time")
                )
            }
            if (partyObject.membersInfo.isEmpty()) return null

            return partyObject
        }
    }
}