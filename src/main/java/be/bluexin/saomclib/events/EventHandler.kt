package be.bluexin.saomclib.events

import be.bluexin.saomclib.SAOMCLib
import be.bluexin.saomclib.capabilities.CapabilitiesExtendedProperty
import be.bluexin.saomclib.capabilities.CapabilitiesHandler
import be.bluexin.saomclib.capabilities.getPartyCapability
import be.bluexin.saomclib.displayNameString
import be.bluexin.saomclib.world
import cpw.mods.fml.common.eventhandler.SubscribeEvent
import cpw.mods.fml.common.network.FMLNetworkEvent
import net.minecraft.client.Minecraft
import net.minecraftforge.client.event.RenderGameOverlayEvent
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.event.entity.EntityEvent
import net.minecraftforge.event.entity.player.PlayerEvent

/**
 *

 * @author Bluexin
 */
internal object EventHandler {



    @SubscribeEvent
    fun entityConstructingEvent(event: EntityEvent.EntityConstructing) {
        if (event.entity.getExtendedProperties(CapabilitiesExtendedProperty.KEY) == null) {
            event.entity.registerExtendedProperties(CapabilitiesExtendedProperty.KEY, CapabilitiesExtendedProperty())
        }
    }

    /*@SubscribeEvent
    fun attachEntityCapabilities(event: AttachCapabilitiesEvent<Entity>) = CapabilitiesHandler.registerEntity(event)

    @SubscribeEvent
    fun attachItemCapabilities(event: AttachCapabilitiesEvent<ItemStack>) = CapabilitiesHandler.registerItem(event)

    @SubscribeEvent
    fun attachTECapabilities(event: AttachCapabilitiesEvent<TileEntity>) = CapabilitiesHandler.registerTE(event)

    @SubscribeEvent
    fun attachWorldCapabilities(event: AttachCapabilitiesEvent<World>) = CapabilitiesHandler.registerWorld(event)*/

    /*@SubscribeEvent
    fun livingTick(e: LivingEvent.LivingUpdateEvent) {
        // TODO: Ticking capabilities?
    }*/

    @SubscribeEvent
    fun cloneEvent(evt: PlayerEvent.Clone) {
        SAOMCLib.LOGGER.info("${evt.entityPlayer} cloned.")
        if (!evt.entityPlayer.world.isRemote) CapabilitiesHandler.restoreEntitiesDeath(evt.entity, evt.original)
    }

    @SubscribeEvent
    fun respawnEvent(evt: cpw.mods.fml.common.gameevent.PlayerEvent.PlayerRespawnEvent) {
        SAOMCLib.LOGGER.info("${evt.player} respawned.")
        if (!evt.player.world.isRemote) CapabilitiesHandler.syncEntitiesDeath(evt.player)
    }

    @SubscribeEvent
    fun playerChangeDimension(evt: cpw.mods.fml.common.gameevent.PlayerEvent.PlayerChangedDimensionEvent) {
        SAOMCLib.LOGGER.info("${evt.player} changed dimension.")
        if (!evt.player.world.isRemote) CapabilitiesHandler.syncEntitiesDimension(evt.player)
    }

    @SubscribeEvent
    fun renderDebugText(evt: RenderGameOverlayEvent.Text) {
        if (!Minecraft.getMinecraft().gameSettings.showDebugInfo) return
        val ptcap = Minecraft.getMinecraft().thePlayer.getPartyCapability()
        evt.left.add("Party: ${ptcap.party}")
        if (ptcap.party != null) {
            evt.left.add(ptcap.party!!.members.joinToString { it.displayNameString })
        }
        evt.left.add("Invited: ${ptcap.invitedTo}")
        if (ptcap.invitedTo != null) {
            evt.left.add(ptcap.invitedTo!!.members.joinToString { it.displayNameString })
        }
    }

    @SubscribeEvent
    fun clientConnectedToServer(event: FMLNetworkEvent.ClientConnectedToServerEvent) {
        Minecraft.getMinecraft().addScheduledTask {
            MinecraftForge.EVENT_BUS.register(JoinServerEvent)
        }
    }

    @SubscribeEvent
    fun playerDisconnect(evt: cpw.mods.fml.common.gameevent.PlayerEvent.PlayerLoggedOutEvent) {
        evt.player.getPartyCapability().clear()
    } // TODO: remove from existing parties, including invites & sync result

}
