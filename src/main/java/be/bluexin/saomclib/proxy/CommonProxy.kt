package be.bluexin.saomclib.proxy

import net.minecraft.entity.player.EntityPlayer
import net.minecraft.util.IThreadListener
import net.minecraft.world.World
import net.minecraftforge.common.util.FakePlayer
import net.minecraftforge.fml.common.FMLCommonHandler
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext
import java.util.*

/**
 * Part of saouintw, the networking mod for the SAO UI

 * @author Bluexin
 */
open class CommonProxy {

    open fun preInit(){

    }

    open fun getPlayerEntity(ctx: MessageContext): EntityPlayer? = ctx.serverHandler.player

    open fun getMinecraftThread(ctx: MessageContext): IThreadListener = ctx.serverHandler.player.server

    open fun getMainWorld(): World = FMLCommonHandler.instance().minecraftServerInstance.getWorld(0)

    open fun getPlayerEntity(uuid: UUID): EntityPlayer? = FMLCommonHandler.instance().minecraftServerInstance.playerList.getPlayerByUUID(uuid)

    open fun getGameProfile(uuid: UUID) = FMLCommonHandler.instance().minecraftServerInstance.playerProfileCache.getProfileByUUID(uuid)

    open fun getPlayerHealth(uuid: UUID): Float {
        val worldServer = FMLCommonHandler.instance().minecraftServerInstance.getWorld(0)
        val player = FakePlayer(worldServer, getGameProfile(uuid))
        return worldServer.saveHandler.playerNBTManager.readPlayerData(player)?.getFloat("Health")?: 0f
    }

    open fun getPlayerMaxHealth(uuid: UUID): Float {
        val worldServer = FMLCommonHandler.instance().minecraftServerInstance.getWorld(0)
        val player = FakePlayer(worldServer, getGameProfile(uuid))
        return worldServer.saveHandler.playerNBTManager.readPlayerData(player)?.getTagList("Attributes", 10)?.getCompoundTagAt(0)?.getDouble("Base")?.toFloat()?: 20f
    }

    open fun isPlayerOnline(uuid: UUID) = FMLCommonHandler.instance().minecraftServerInstance.playerList.onlinePlayerProfiles.any { it.id == uuid }

    open fun getSide() = ProxySide.SERVER

    open var isServerSideLoaded: Boolean = true

    enum class ProxySide {
        CLIENT,
        SERVER;
    }
}
