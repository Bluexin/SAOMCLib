package be.bluexin.saomclib.events

import be.bluexin.saomclib.SAOMCLib
import net.minecraftforge.common.MinecraftForge
import net.minecraft.client.entity.EntityPlayerSP
import net.minecraftforge.event.entity.EntityJoinWorldEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent



object JoinServerEvent {

    @SubscribeEvent
    fun entityJoinWorld(event: EntityJoinWorldEvent) {
        if (event.entity is EntityPlayerSP) {
            SAOMCLib.LOGGER.info("Logged in, requesting sync packet")

            val player = event.entity as EntityPlayerSP
            player.sendChatMessage("/saomc sync")

            MinecraftForge.EVENT_BUS.unregister(this)
        }
    }
}