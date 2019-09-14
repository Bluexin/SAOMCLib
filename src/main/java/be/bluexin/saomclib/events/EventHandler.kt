package be.bluexin.saomclib.events

import be.bluexin.saomclib.capabilities.CapabilitiesHandler
import be.bluexin.saomclib.capabilities.getPartyCapability
import net.minecraft.client.Minecraft
import net.minecraft.entity.Entity
import net.minecraft.item.ItemStack
import net.minecraft.tileentity.TileEntity
import net.minecraft.world.World
import net.minecraftforge.client.event.RenderGameOverlayEvent
import net.minecraftforge.event.AttachCapabilitiesEvent
import net.minecraftforge.event.entity.player.PlayerEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

/**
 *

 * @author Bluexin
 */
internal object EventHandler {

    @SubscribeEvent
    fun attachEntityCapabilities(event: AttachCapabilitiesEvent<Entity>) = CapabilitiesHandler.registerEntity(event)

    @SubscribeEvent
    fun attachItemCapabilities(event: AttachCapabilitiesEvent<ItemStack>) = CapabilitiesHandler.registerItem(event)

    @SubscribeEvent
    fun attachTECapabilities(event: AttachCapabilitiesEvent<TileEntity>) = CapabilitiesHandler.registerTE(event)

    @SubscribeEvent
    fun attachWorldCapabilities(event: AttachCapabilitiesEvent<World>) = CapabilitiesHandler.registerWorld(event)

    /*@SubscribeEvent
    fun livingTick(e: LivingEvent.LivingUpdateEvent) {
        // TODO: Ticking capabilities?
    }*/

    @SubscribeEvent
    fun cloneEvent(evt: PlayerEvent.Clone) {
        if (!evt.entityPlayer.world.isRemote) CapabilitiesHandler.restoreEntitiesDeath(evt.entity, evt.original)
    }

    @SubscribeEvent
    fun respawnEvent(evt: net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerRespawnEvent) {
        if (!evt.player.world.isRemote) CapabilitiesHandler.syncEntitiesDeath(evt.player)
    }

    @SubscribeEvent
    fun playerChangeDimension(evt: net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerChangedDimensionEvent) {
        if (!evt.player.world.isRemote) CapabilitiesHandler.syncEntitiesDimension(evt.player)
    }

    @SubscribeEvent
    fun renderDebugText(evt: RenderGameOverlayEvent.Text) {
        if (!Minecraft.getMinecraft().gameSettings.showDebugInfo) return
        val ptcap = Minecraft.getMinecraft().player.getPartyCapability()
        evt.left.add("Party: ${ptcap.party}")
        if (ptcap.party != null) {
            evt.left.add(ptcap.party!!.membersInfo.joinToString { it.username } + " " + ptcap.party!!.invitedInfo.joinToString { "+${it.key.username}" })
        }
        evt.left.add("Invited: ${ptcap.invitedTo}")
        if (ptcap.invitedTo != null) {
            evt.left.add(ptcap.invitedTo!!.membersInfo.joinToString { it.username } + " " + ptcap.invitedTo!!.invitedInfo.joinToString { "+${it.key.username}" })
        }
    }

    @SubscribeEvent
    fun playerDisconnect(evt: net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedOutEvent) {
        evt.player.getPartyCapability().clear()
    }

}
