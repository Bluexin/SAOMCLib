package be.bluexin.saomclib.events

import be.bluexin.saomclib.SAOMCLib
import cpw.mods.fml.client.FMLClientHandler
import cpw.mods.fml.common.eventhandler.SubscribeEvent
import net.minecraft.client.entity.EntityPlayerSP
import net.minecraft.client.network.NetHandlerPlayClient
import net.minecraft.network.play.client.C01PacketChatMessage
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.event.entity.EntityJoinWorldEvent

object JoinServerEvent {

    @SubscribeEvent
    fun entityJoinWorld(event: EntityJoinWorldEvent) {
        if (event.entity is EntityPlayerSP) {
            SAOMCLib.LOGGER.info("Logged in, requesting sync packet")

            (FMLClientHandler.instance().clientPlayHandler as? NetHandlerPlayClient)?.addToSendQueue(C01PacketChatMessage("/saomc sync"))

            MinecraftForge.EVENT_BUS.unregister(this)
        }
    }
}