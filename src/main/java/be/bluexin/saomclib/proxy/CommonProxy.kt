package be.bluexin.saomclib.proxy

import net.minecraft.entity.player.EntityPlayer
import net.minecraft.util.IThreadListener
import net.minecraft.world.World
import net.minecraftforge.fml.common.FMLCommonHandler
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext
import java.util.*

/**
 * Part of saouintw, the networking mod for the SAO UI

 * @author Bluexin
 */
internal open class CommonProxy {

    open fun preInit(){
    }

    open fun getPlayerEntity(ctx: MessageContext): EntityPlayer? = ctx.serverHandler.player

    open fun getMinecraftThread(ctx: MessageContext): IThreadListener = ctx.serverHandler.player.server

    open fun getMainWorld(): World = FMLCommonHandler.instance().minecraftServerInstance.getWorld(0)

    open fun getPlayerEntity(uuid: UUID): EntityPlayer? = FMLCommonHandler.instance().minecraftServerInstance.playerList.getPlayerByUUID(uuid)

    open fun getGameProfile(uuid: UUID) = FMLCommonHandler.instance().minecraftServerInstance.playerProfileCache.getProfileByUUID(uuid)

    open fun getSide() = ProxySide.SERVER

    enum class ProxySide {
        CLIENT,
        SERVER;
    }
}
