package com.tencao.saomclib.packets

import com.tencao.saomclib.*
import com.tencao.saomclib.capabilities.AbstractEntityCapability
import com.tencao.saomclib.capabilities.CapabilitiesHandler
import io.netty.buffer.ByteBuf
import net.minecraft.entity.Entity
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.IThreadListener
import net.minecraft.util.ResourceLocation
import net.minecraftforge.fml.common.network.simpleimpl.IMessage
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext
import java.util.*

/**
 * Syncs a capability for an Entity.
 * Huge capabilities should be either split into smaller ones, or handled manually in separate packets.
 * Should only ever be called server side
 *
 * @author Bluexin
 */
class SyncEntityCapabilityPacket() : IMessage {

    private lateinit var capabilityID: String
    private lateinit var data: NBTTagCompound
    private lateinit var targetUUID: UUID

    constructor(capability: AbstractEntityCapability, target: Entity) : this() {
        val rl = CapabilitiesHandler.getID(capability.javaClass)
        capabilityID = rl.toString()
        data = CapabilitiesHandler.getEntityCapability(rl).writeNBT(capability, null) as NBTTagCompound
        targetUUID = target.uniqueID
    }

    override fun fromBytes(buffer: ByteBuf) {
        capabilityID = buffer.readString()
        data = buffer.readTag()
        targetUUID = UUID.fromString(buffer.readString())
    }

    override fun toBytes(buffer: ByteBuf) {
        buffer.writeString(capabilityID)
        buffer.writeTag(data)
        buffer.writeString(targetUUID.toString())
    }

    companion object {
        class Handler : AbstractClientPacketHandler<SyncEntityCapabilityPacket>() {
            override fun handleClientPacket(player: EntityPlayer, message: SyncEntityCapabilityPacket, ctx: MessageContext, mainThread: IThreadListener): IMessage? {
                mainThread.addScheduledTask {
                    val cap = CapabilitiesHandler.getEntityCapability(ResourceLocation(message.capabilityID))
                    try {
                        cap.readNBT(player.world.loadedEntityList.single { it.uniqueID == message.targetUUID }.getCapability(cap, null), null, message.data)
                    } catch (e: Exception) {
                        SAOMCLib.LOGGER.info("[SyncEntityCapabilityPacket] Suppressed an error.\nPacket content: ${message.capabilityID}, ${message.targetUUID}, ${message.data}\nPlayers: ${player.world.playerEntities}", e)
                    }
                }

                return null
            }
        }
    }
}
