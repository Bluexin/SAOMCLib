package com.tencao.saomclib.events

import com.tencao.saomclib.capabilities.getBlockRecords
import com.tencao.saomclib.onServer
import net.minecraftforge.event.world.BlockEvent
import net.minecraftforge.fml.common.eventhandler.EventPriority
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

/**
 * The goal of this is to monitor all block modification
 * and log it
 */
internal object BlockMonitor {

    @SubscribeEvent(priority = EventPriority.LOWEST)
    fun onBlockBreak(evt: BlockEvent.BreakEvent) {
        evt.world.onServer {
            evt.world.getChunkFromBlockCoords(evt.pos).getBlockRecords().addBlockPos(evt.pos)
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    fun onBlockBreak(evt: BlockEvent.PlaceEvent) {
        evt.world.onServer {
            evt.world.getChunkFromBlockCoords(evt.pos).getBlockRecords().addBlockPos(evt.pos)
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    fun onBlockBreak(evt: BlockEvent.MultiPlaceEvent) {
        evt.world.onServer {
            evt.replacedBlockSnapshots.forEach {
                evt.world.getChunkFromBlockCoords(it.pos).getBlockRecords().addBlockPos(it.pos)
            }
        }
    }
}
