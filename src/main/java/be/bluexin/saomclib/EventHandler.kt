package be.bluexin.saomclib

import be.bluexin.saomclib.capabilities.CapabilitiesHandler
import be.bluexin.saomclib.capabilities.getPartyCapability
import cpw.mods.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.event.entity.EntityEvent
import net.minecraftforge.event.entity.living.LivingEvent
import net.minecraftforge.event.entity.player.PlayerEvent

/**
 *
 *
 * @author Bluexin
 */
internal class EventHandler {

    @SubscribeEvent
    fun attachEntityCapabilities(event: EntityEvent.EntityConstructing) = CapabilitiesHandler.registerEntity(event)

    @SubscribeEvent
    fun livingTick(e: LivingEvent.LivingUpdateEvent) {
        // TODO: Ticking capabilities?
//        if (e.entityLiving.hasCapability(RenderCapability.RENDER_CAPABILITY, null))
//            RenderCapability.get(e.entityLiving).colorStateHandler.tick()
    }

    @SubscribeEvent
    fun cloneEvent(evt: PlayerEvent.Clone) {
        LogHelper.logInfo("${evt.entityPlayer} cloned.")
        if (!evt.entityPlayer.worldObj.isRemote) CapabilitiesHandler.restoreEntitiesDeath(evt.entity, evt.original)
    }

    @SubscribeEvent
    fun respawnEvent(evt: cpw.mods.fml.common.gameevent.PlayerEvent.PlayerRespawnEvent) {
        LogHelper.logInfo("${evt.player} respawned.")
        if (!evt.player.worldObj.isRemote) CapabilitiesHandler.syncEntitiesDeath(evt.player)
    }

    @SubscribeEvent
    fun playerChangeDimension(evt: cpw.mods.fml.common.gameevent.PlayerEvent.PlayerChangedDimensionEvent) {
        LogHelper.logInfo("${evt.player} changed dimension.")
        if (!evt.player.worldObj.isRemote) CapabilitiesHandler.syncEntitiesDimension(evt.player)
    }

    @SubscribeEvent
    fun playerConnect(evt: cpw.mods.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent) {
        LogHelper.logInfo("${evt.player} logged in.")
        if (!evt.player.worldObj.isRemote) CapabilitiesHandler.syncEntitiesLogin(evt.player)
    }

    @SubscribeEvent
    fun playerDisconnect(evt: cpw.mods.fml.common.gameevent.PlayerEvent.PlayerLoggedOutEvent) {
        evt.player.getPartyCapability().clear()
    }

}
