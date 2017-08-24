package be.bluexin.saomclib.events

import be.bluexin.saomclib.SAOMCLib
import be.bluexin.saomclib.capabilities.CapabilitiesHandler
import be.bluexin.saomclib.capabilities.getPartyCapability
import net.minecraft.client.Minecraft
import net.minecraft.entity.Entity
import net.minecraft.item.Item
import net.minecraft.tileentity.TileEntity
import net.minecraft.world.World
import net.minecraftforge.client.ClientCommandHandler
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.event.AttachCapabilitiesEvent
import net.minecraftforge.event.entity.living.LivingEvent
import net.minecraftforge.event.entity.player.PlayerEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.network.FMLNetworkEvent

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
    }

    @SubscribeEvent
    fun cloneEvent(evt: PlayerEvent.Clone) {
        SAOMCLib.LOGGER.info("${evt.entityPlayer} cloned.")
        if (!evt.entityPlayer.world.isRemote) CapabilitiesHandler.restoreEntitiesDeath(evt.entity, evt.original)
    }

    @SubscribeEvent
    fun respawnEvent(evt: net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerRespawnEvent) {
        SAOMCLib.LOGGER.info("${evt.player} respawned.")
        if (!evt.player.world.isRemote) CapabilitiesHandler.syncEntitiesDeath(evt.player)
    }

    @SubscribeEvent
    fun playerChangeDimension(evt: net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerChangedDimensionEvent) {
        SAOMCLib.LOGGER.info("${evt.player} changed dimension.")
        if (!evt.player.world.isRemote) CapabilitiesHandler.syncEntitiesDimension(evt.player)
    }

    @SubscribeEvent
    fun clientConnectedToServer(event: FMLNetworkEvent.ClientConnectedToServerEvent) {
        Minecraft.getMinecraft().addScheduledTask {
            MinecraftForge.EVENT_BUS.register(JoinServerEvent)
        }
    }

    /*
    @SubscribeEvent
    fun playerConnect(evt: FMLNetworkEvent.ClientConnectedToServerEvent){
        SAOMCLib.LOGGER.info("Logged in, requesting sync packet")
        SAOMCLib.LOGGER.info("Player: " + Minecraft.getMinecraft().player)
        ClientCommandHandler.instance.executeCommand(Minecraft.getMinecraft().player, SAOMCLib.MODID + " sync")
    }*/

    @SubscribeEvent
    fun playerDisconnect(evt: net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedOutEvent) {
        evt.player.getPartyCapability().clear()
    }

}
