package com.tencao.saomclib.events

import com.tencao.saomclib.SAOMCLib
import net.minecraftforge.client.event.ClientPlayerNetworkEvent
import net.minecraftforge.eventbus.api.SubscribeEvent

object ClientEventListener {

    @SubscribeEvent
    fun clientDisconnect(evt: ClientPlayerNetworkEvent.LoggedOutEvent) {
        SAOMCLib.proxy.isServerSideLoaded = false
    }
}
