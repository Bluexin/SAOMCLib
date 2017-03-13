package be.bluexin.saomclib.proxy

import cpw.mods.fml.common.network.simpleimpl.MessageContext
import net.minecraft.client.Minecraft
import net.minecraft.entity.player.EntityPlayer

/**
 * Part of saouintw, the networking mod for the SAO UI

 * @author Bluexin
 */
@Suppress("unused")
internal class ClientProxy : CommonProxy() {

    override fun getPlayerEntity(ctx: MessageContext): EntityPlayer? = if (ctx.side.isClient) Minecraft.getMinecraft().thePlayer else super.getPlayerEntity(ctx)
}
