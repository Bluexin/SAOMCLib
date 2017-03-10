package be.bluexin.saomclib.packets

import be.bluexin.saomclib.*
import be.bluexin.saomclib.capabilities.AbstractEntityCapability
import be.bluexin.saomclib.capabilities.CapabilitiesHandler
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
 *
 * @author Bluexin
 */
class SyncEntityCapabilityPacket() : IMessage {

    private lateinit var data: NBTTagCompound
    private lateinit var targetUUID: UUID
    private lateinit var capabilityID: String

    @Suppress("unused")
    constructor(capability: AbstractEntityCapability, target: Entity) : this() {
        val rl = CapabilitiesHandler.getID(capability.javaClass)
        this.capabilityID = rl.toString()
        this.data = CapabilitiesHandler.getEntityCapability(rl).writeNBT(capability, null) as NBTTagCompound
        this.targetUUID = target.uniqueID
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
                        cap.readNBT(player.world.loadedEntityList.filter { it.uniqueID == message.targetUUID }.single().getCapability(cap, null), null, message.data)
                    } catch (e: Exception) {
                        LogHelper.logInfo("Suppressed an error.")
                    }
                }

                return null
            }
        }
    }
}
