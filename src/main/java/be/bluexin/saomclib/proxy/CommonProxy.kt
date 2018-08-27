package be.bluexin.saomclib.proxy

import net.minecraft.entity.player.EntityPlayer
import net.minecraft.util.IThreadListener
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext

/**
 * Part of saouintw, the networking mod for the SAO UI

 * @author Bluexin
 */
internal open class CommonProxy {

    open fun preInit(){
    }

    open fun getPlayerEntity(ctx: MessageContext): EntityPlayer? = ctx.serverHandler.player

    open fun getMinecraftThread(ctx: MessageContext): IThreadListener = ctx.serverHandler.player.server
}
