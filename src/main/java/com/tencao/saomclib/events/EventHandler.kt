package com.tencao.saomclib.events

import com.tencao.saomclib.capabilities.CapabilitiesHandler
import com.tencao.saomclib.capabilities.getPartyCapability
import com.tencao.saomclib.onServer
import com.tencao.saomclib.packets.MakeClientAwarePacket
import com.tencao.saomclib.party.PartyManager
import com.tencao.saomclib.party.playerInfo
import com.tencao.saomclib.sendPacket
import com.tencao.saomclib.utils.ModHelper
import net.minecraft.client.Minecraft
import net.minecraft.entity.Entity
import net.minecraft.entity.player.EntityPlayerMP
import net.minecraft.item.ItemStack
import net.minecraft.tileentity.TileEntity
import net.minecraft.world.World
import net.minecraft.world.chunk.Chunk
import net.minecraftforge.client.event.RenderGameOverlayEvent
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

    @SubscribeEvent
    fun attachChunkCapabilities(event: AttachCapabilitiesEvent<Chunk>) = CapabilitiesHandler.registerChunk(event)

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
            evt.left.add(ptcap.partyData!!.membersInfo.joinToString { it.username } + " " + ptcap.partyData!!.invitedInfo.map { it.key }.joinToString { "+${it.username}" })
        }
        evt.left.add("Invited: ${ptcap.inviteData}")
        ptcap.inviteData.forEach { inviteData ->
            evt.left.add(inviteData.getMembers().joinToString { it.username } + " " + inviteData.invitedInfo.map { it.key }.joinToString { "+${it.username}" })
        }
    }

    @SubscribeEvent
    fun playerDisconnect(evt: net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedOutEvent) {
        PartyManager.getInvitedParties(evt.player.playerInfo()).forEach { it.cancel(evt.player) }
        if (!ModHelper.isTogetherForeverLoaded) {
            evt.player.world.onServer {
                if (PartyManager.getPartyObject(evt.player.playerInfo())?.isParty == false) {
                    PartyManager.removeParty(PartyManager.getPartyObject(evt.player.playerInfo())!!)
                }
            }
        }
    }

    @SubscribeEvent
    fun playerConnect(evt: net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent) {
        evt.player.world.onServer {
            evt.player.server?.addScheduledTask {
                PartyManager.getPartyObject(evt.player.playerInfo())?.sync(evt.player)
                CapabilitiesHandler.syncEntitiesLogin(evt.player)
                (evt.player as EntityPlayerMP).sendPacket(MakeClientAwarePacket())
            }
        }
    }

    @SubscribeEvent
    fun clearInvites(evt: TickEvent.ServerTickEvent) {
        if (evt.phase == TickEvent.Phase.END) {
            PartyManager.parties.forEach { if (it.cleanupInvites()) it.dissolve() }
            /*
            val parties = PartyManager.getParties()
            while(parties.hasNext()){
                val party = parties.next()
                if (party.cleanupInvites()){
                    party.dissolve()
                }
            }*/
        }
    }
}
