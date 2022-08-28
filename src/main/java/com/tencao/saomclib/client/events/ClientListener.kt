package com.tencao.saomclib.client.events

import com.tencao.saomclib.SAOMCLib
import com.tencao.saomclib.capabilities.getPartyCapability
import net.minecraft.client.Minecraft
import net.minecraftforge.api.distmarker.Dist
import net.minecraftforge.client.event.ClientPlayerNetworkEvent
import net.minecraftforge.client.event.RenderGameOverlayEvent
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.fml.common.Mod

@Mod.EventBusSubscriber(modid = SAOMCLib.MODID, value = [Dist.CLIENT], bus = Mod.EventBusSubscriber.Bus.MOD)
internal object ClientListener {

    @SubscribeEvent
    fun clientDisconnect(e: ClientPlayerNetworkEvent.LoggedOutEvent) {
        SAOMCLib.proxy.isServerSideLoaded = false
    }

    @SubscribeEvent
    fun renderDebugText(evt: RenderGameOverlayEvent.Text) {
        if (!Minecraft.getInstance().gameSettings.showDebugInfo) return
        val ptcap = Minecraft.getInstance().player?.getPartyCapability() ?: return
        evt.left.add("Party: ${ptcap.partyData}")
        if (ptcap.partyData != null) {
            evt.left.add(ptcap.partyData!!.membersInfo.joinToString { it.username } + " " + ptcap.partyData!!.invitedInfo.map { it.key }.joinToString { "+${it.username}" })
        }
        evt.left.add("Invited: ${ptcap.inviteData}")
        ptcap.inviteData.forEach { inviteData ->
            evt.left.add(inviteData.getMembers().joinToString { it.username } + " " + inviteData.invitedInfo.map { it.key }.joinToString { "+${it.username}" })
        }
    }
}
