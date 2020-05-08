package be.bluexin.saomclib.events

import be.bluexin.saomclib.capabilities.CapabilitiesHandler
import be.bluexin.saomclib.capabilities.getPartyCapability
import be.bluexin.saomclib.onServer
import be.bluexin.saomclib.party.PartyManager
import be.bluexin.saomclib.utils.ModHelper
import net.minecraft.client.Minecraft
import net.minecraft.entity.Entity
import net.minecraft.item.ItemStack
import net.minecraft.tileentity.TileEntity
import net.minecraft.world.World
import net.minecraftforge.client.event.RenderGameOverlayEvent
import net.minecraftforge.common.DimensionManager
import net.minecraftforge.event.AttachCapabilitiesEvent
import net.minecraftforge.event.entity.player.PlayerEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent


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
        evt.left.add("Party: ${ptcap.partyData}")
        if (ptcap.partyData != null) {
            evt.left.add(ptcap.partyData!!.membersInfo.joinToString { it.username } + " " + ptcap.partyData!!.invitedInfo.keys.joinToString { "+${it.username}" })
        }
        evt.left.add("Invited: ${ptcap.inviteData}")
        ptcap.inviteData.forEach { inviteData ->
            evt.left.add(inviteData.membersInfo.joinToString { it.username } + " " + inviteData.invitedInfo.keys.joinToString { "+${it.username}" })
        }
    }

    @SubscribeEvent
    fun playerDisconnect(evt: net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedOutEvent) {
        PartyManager.getInvitedParty(evt.player)?.cancel(evt.player)
        if (!ModHelper.isTogetherForeverLoaded) {
            evt.player.world.onServer {
                if (PartyManager.getPartyObject(evt.player)?.isParty == false)
                    PartyManager.removeParty(PartyManager.getPartyObject(evt.player)!!)
            }
        }
    }

    @SubscribeEvent
    fun playerConnect(evt: net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent){
        PartyManager.getPartyObject(evt.player)?.sync(evt.player)
    }

    @SubscribeEvent
    fun clearInvites(evt: TickEvent.WorldTickEvent){
        if (evt.phase == TickEvent.Phase.END) {
            evt.world.onServer {
                if (evt.world == DimensionManager.getWorld(0)) {
                    val parties = PartyManager.getPartyIterator()
                    while (parties.hasNext()) {
                        parties.next().cleanupInvites(evt.world.totalWorldTime)
                    }
                }
            }
        }
    }

}
