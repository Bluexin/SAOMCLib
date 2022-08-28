package com.tencao.saomclib.events

import com.tencao.saomclib.capabilities.getBlockRecords
import com.tencao.saomclib.onServer
import net.minecraftforge.api.distmarker.Dist
import net.minecraftforge.event.world.BlockEvent
import net.minecraftforge.eventbus.api.EventPriority
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.fml.common.Mod

/**
 * The goal of this is to monitor all block modification
 * and log it
 */

@Mod.EventBusSubscriber(Dist.DEDICATED_SERVER)
internal object BlockListener {

    @SubscribeEvent(priority = EventPriority.LOWEST)
    fun onBlockBreak(evt: BlockEvent.BreakEvent) {
        evt.world.onServer {
            evt.world.getBlockRecords(evt.pos)?.addBlockPos(evt.pos)
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    fun onBlockBreak(evt: BlockEvent.EntityPlaceEvent) {
        evt.world.onServer {
            evt.world.getBlockRecords(evt.pos)?.addBlockPos(evt.pos)
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    fun onBlockBreak(evt: BlockEvent.EntityMultiPlaceEvent) {
        evt.world.onServer {
            evt.replacedBlockSnapshots.forEach {
                evt.world.getBlockRecords(evt.pos)?.addBlockPos(evt.pos)
            }
        }
    }
}
