package com.tencao.saomclib.capabilities

import com.tencao.saomclib.packets.SyncEntityCapabilityPacket
import com.tencao.saomclib.sendPacket
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
        (ent as? EntityPlayerMP)?.sendPacket(SyncEntityCapabilityPacket(this, ent))
    }

    open val shouldSyncOnDeath = true

    open val shouldSyncOnDimensionChange = true

    open val shouldRestoreOnDeath = true

    open val shouldSendOnLogin = true

    /**
     * Called to restore the capability upon death.
     * Only called on the server by the lib, forcing this call should respect that.
     * If you set your storage properly, this implementation should be plenty.
     * Provided for more customization.
     * Called before the default handler. To prevent the default handler, return false.
     *
     * @return true if the default handler should still proceed
     */
    open fun restore(entity: Entity, original: Entity) = true
}
