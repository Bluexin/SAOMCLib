package com.tencao.saomclib.events

import com.tencao.saomclib.capabilities.CapabilitiesHandler
import com.tencao.saomclib.onServer
import com.tencao.saomclib.packets.toClient.MakeClientAwarePacket
import com.tencao.saomclib.party.PartyManager
import com.tencao.saomclib.party.playerInfo
import com.tencao.saomclib.sendPacket
import net.minecraft.entity.Entity
import net.minecraft.entity.player.ServerPlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.tileentity.TileEntity
import net.minecraft.world.World
import net.minecraft.world.chunk.Chunk
import net.minecraftforge.event.AttachCapabilitiesEvent
import net.minecraftforge.event.TickEvent
import net.minecraftforge.event.entity.player.PlayerEvent
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.fml.common.Mod

/**
 *
 * @author Bluexin
 */

@Mod.EventBusSubscriber
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
        if (evt.entityLiving.isServerWorld) CapabilitiesHandler.restoreEntitiesDeath(evt.entity, evt.original)
    }

    @SubscribeEvent
    fun respawnEvent(evt: PlayerEvent.PlayerRespawnEvent) {
        if (!evt.player.world.isRemote) CapabilitiesHandler.syncEntitiesDeath(evt.player)
    }

    @SubscribeEvent
    fun playerChangeDimension(evt: PlayerEvent.PlayerChangedDimensionEvent) {
        if (!evt.player.world.isRemote) CapabilitiesHandler.syncEntitiesDimension(evt.player)
    }

    @SubscribeEvent
    fun playerDisconnect(evt: PlayerEvent.PlayerLoggedOutEvent) {
        PartyManager.getInvitedParty(evt.player.playerInfo())?.cancel(evt.player)
        // Just incase
        /*
        evt.player.world.onServer {
            PacketPipeline.sendTo(MakeClientAwarePacket(false), evt.player as ServerPlayerEntity)
        }*/
    }

    @SubscribeEvent
    fun playerConnect(evt: PlayerEvent.PlayerLoggedInEvent) {
        evt.player.world.onServer {
            evt.player.server?.deferTask {
                PartyManager.getPartyObject(evt.player.playerInfo())?.sync(evt.player)
                CapabilitiesHandler.syncEntitiesLogin(evt.player)
                (evt.player as ServerPlayerEntity).sendPacket(MakeClientAwarePacket())
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
