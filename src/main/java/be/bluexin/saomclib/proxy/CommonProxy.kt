package be.bluexin.saomclib.proxy

import cpw.mods.fml.common.network.simpleimpl.MessageContext
import net.minecraft.entity.player.EntityPlayer

/**
 * Part of saouintw, the networking mod for the SAO UI

 * @author Bluexin
 */
internal open class CommonProxy {

    open fun getPlayerEntity(ctx: MessageContext): EntityPlayer? = ctx.serverHandler.playerEntity
}
