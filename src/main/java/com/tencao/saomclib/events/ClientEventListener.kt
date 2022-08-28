package com.tencao.saomclib.events

import com.tencao.saomclib.SAOMCLib
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.network.FMLNetworkEvent.ClientDisconnectionFromServerEvent

object ClientEventListener {

    @SubscribeEvent
    fun clientDisconnect(evt: ClientDisconnectionFromServerEvent) {
        SAOMCLib.proxy.isServerSideLoaded = false
    }
}
