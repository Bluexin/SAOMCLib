package be.bluexin.saomclib.capabilities

import be.bluexin.saomclib.packets.SyncEntityCapabilityPacket
import be.bluexin.saomclib.sendPacket
import net.minecraft.entity.Entity
import net.minecraft.entity.player.EntityPlayerMP
import net.minecraft.world.World
import java.lang.ref.WeakReference

/**
 * Part of saomclib by Bluexin.
 *
 * @sample be.bluexin.saomclib.example.SimpleCapability for a Kotlin example
 * @sample be.bluexin.saomclib.example.JSimpleCapability for a Java example
 *
 * @author Bluexin
 */
abstract class AbstractEntityCapability : AbstractCapability() {

    protected lateinit var reference: WeakReference<Entity>

    override fun setup(param: Entity): AbstractCapability {
        return this
    }

    override fun init(entity: Entity, world: World?) {
        reference = WeakReference(entity)
        setup(entity)
    }

    /**
     * Will be called when the EventHandler thinks this should be synced,
     * depending on [shouldSyncOnDeath], [shouldSyncOnDimensionChange], [shouldRestoreOnDeath] and [shouldSendOnLogin].
     * Only called on the server by the lib, forcing this call should respect that.
     * If you set your storage properly, this implementation should be plenty.
     */
    open fun sync() {
        val ent = reference.get()
        if (ent is EntityPlayerMP) ent.sendPacket(SyncEntityCapabilityPacket(this, ent))
    }

    open val shouldSyncOnDeath = true

    open val shouldSyncOnDimensionChange = true

    open val shouldRestoreOnDeath = true

    open val shouldSendOnLogin = true
}
