package com.tencao.saomclib.packets.toClient

import com.tencao.saomclib.SAOMCLib
import com.tencao.saomclib.capabilities.AbstractEntityCapability
import com.tencao.saomclib.capabilities.CapabilitiesHandler
import com.tencao.saomclib.packets.IPacket
import net.minecraft.client.Minecraft
import net.minecraft.entity.Entity
import net.minecraft.nbt.CompoundNBT
import net.minecraft.network.PacketBuffer
import net.minecraft.util.ResourceLocation
import net.minecraftforge.fml.network.NetworkEvent
import java.util.*

/**
 * Syncs a capability for an Entity.
 * Huge capabilities should be either split into smaller ones, or handled manually in separate packets.
 * Should only ever be called server side
 *
 * @author Bluexin
 */
class SyncEntityCapabilityPacket() : IPacket {

    private lateinit var capabilityID: String
    private lateinit var data: CompoundNBT
    private lateinit var targetUUID: UUID

    constructor(capability: AbstractEntityCapability, target: Entity) : this() {
        val rl = CapabilitiesHandler.getID(capability.javaClass)
        capabilityID = rl.toString()
        data = CapabilitiesHandler.getEntityCapability(rl).writeNBT(capability, null) as CompoundNBT
        targetUUID = target.uniqueID
    }

    constructor(capabilityID: String, data: CompoundNBT, targetUUID: UUID) : this() {
        this.capabilityID = capabilityID
        this.data = data
        this.targetUUID = targetUUID
    }

    override fun handle(context: NetworkEvent.Context) {
        val cap = CapabilitiesHandler.getEntityCapability(ResourceLocation(capabilityID))
        try {
            Minecraft.getInstance().world?.allEntities?.forEach {
                if (it.uniqueID == targetUUID) {
                    cap.readNBT(it.getCapability(cap, null).resolve().get(), null, data)
                }
            }
        } catch (e: Exception) {
            SAOMCLib.LOGGER.info("[SyncEntityCapabilityPacket] Suppressed an error.\nPacket content: $capabilityID, $targetUUID, ${data}\nPlayers: ${Minecraft.getInstance().world?.players}", e)
        }
    }

    override fun encode(buffer: PacketBuffer) {
        buffer.writeString(capabilityID)
        buffer.writeCompoundTag(data)
        buffer.writeString(targetUUID.toString())
    }

    companion object {

        fun decode(buffer: PacketBuffer): SyncEntityCapabilityPacket {
            return SyncEntityCapabilityPacket(
                buffer.readString(),
                buffer.readCompoundTag() ?: CompoundNBT(),
                UUID.fromString(buffer.readString())
            )
        }
    }
}
