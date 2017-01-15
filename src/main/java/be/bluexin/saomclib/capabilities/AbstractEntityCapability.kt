package be.bluexin.saomclib.capabilities

import be.bluexin.saomclib.packets.PacketPipeline
import be.bluexin.saomclib.packets.SyncEntityCapabilityPacket
import net.minecraft.entity.Entity
import net.minecraft.entity.player.EntityPlayerMP
import java.lang.ref.WeakReference

/**
 * Part of saomclib by Bluexin.
 *
 * @author Bluexin
 */
abstract class AbstractEntityCapability : AbstractCapability() {

    protected lateinit var reference: WeakReference<Entity>

    override fun setup(param: Any): AbstractCapability {
        reference = WeakReference(param as Entity)
        return this
    }

    /**
     * Will be called when the EventHandler thinks this should be synced,
     * depending on [shouldSyncOnDeath], [shouldSyncOnDimensionChange], [shouldRestoreOnDeath] and [shouldSendOnLogin].
     * Only called on the server by the lib, forcing this call should respect that.
     * If you set your storage properly, this implementation should be plenty.
     */
    open fun sync() {
        val ent = reference.get()
        if (ent is EntityPlayerMP) PacketPipeline.sendTo(SyncEntityCapabilityPacket(this, ent), ent)
    }

    open val shouldSyncOnDeath = true

    open val shouldSyncOnDimensionChange = true

    open val shouldRestoreOnDeath = true

    open val shouldSendOnLogin = true
}
