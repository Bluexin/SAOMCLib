package be.bluexin.saomclib

import be.bluexin.saomclib.capabilities.CapabilitiesHandler
import net.minecraft.entity.Entity
import net.minecraft.item.Item
import net.minecraft.tileentity.TileEntity
import net.minecraft.world.World
import net.minecraftforge.event.AttachCapabilitiesEvent
import net.minecraftforge.event.entity.living.LivingEvent
import net.minecraftforge.event.entity.player.PlayerEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent


/**
 *

 * @author Bluexin
 */
internal class EventHandler {

    @SubscribeEvent
    fun attachEntityCapabilities(event: AttachCapabilitiesEvent<Entity>) = CapabilitiesHandler.registerEntity(event)

    @SubscribeEvent
    fun attachItemCapabilities(event: AttachCapabilitiesEvent<Item>) = CapabilitiesHandler.registerItem(event)

    @SubscribeEvent
    fun attachTECapabilities(event: AttachCapabilitiesEvent<TileEntity>) = CapabilitiesHandler.registerTE(event)

    @SubscribeEvent
    fun attachWorldCapabilities(event: AttachCapabilitiesEvent<World>) = CapabilitiesHandler.registerWorld(event)

    @SubscribeEvent
    fun livingTick(e: LivingEvent.LivingUpdateEvent) {
        // TODO: Ticking capabilities?
//        if (e.entityLiving.hasCapability(RenderCapability.RENDER_CAPABILITY, null))
//            RenderCapability.get(e.entityLiving).colorStateHandler.tick()
    }

    @SubscribeEvent
    fun cloneEvent(evt: PlayerEvent.Clone) {
        LogHelper.logInfo("${evt.entityPlayer} cloned.")
        if (!evt.entityPlayer.world.isRemote) CapabilitiesHandler.restoreEntitiesDeath(evt.entity, evt.original)
    }

    @SubscribeEvent
    fun respawnEvent(evt: net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerRespawnEvent) {
        LogHelper.logInfo("${evt.player} respawned.")
        if (!evt.player.world.isRemote) CapabilitiesHandler.syncEntitiesDeath(evt.player)
    }

    @SubscribeEvent
    fun playerChangeDimension(evt: net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerChangedDimensionEvent) {
        LogHelper.logInfo("${evt.player} changed dimension.")
        if (!evt.player.world.isRemote) CapabilitiesHandler.syncEntitiesDimension(evt.player)
    }

    @SubscribeEvent
    fun playerConnect(evt: net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent) {
        LogHelper.logInfo("${evt.player} logged in.")
        if (!evt.player.world.isRemote) CapabilitiesHandler.syncEntitiesLogin(evt.player)
    }

}
