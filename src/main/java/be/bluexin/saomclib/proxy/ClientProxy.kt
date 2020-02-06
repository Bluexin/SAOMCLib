package be.bluexin.saomclib.proxy

import net.minecraft.client.Minecraft
import net.minecraft.client.entity.EntityOtherPlayerMP
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.util.IThreadListener
import net.minecraft.world.World
import net.minecraftforge.fml.client.FMLClientHandler
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext
import java.util.*

/**
 * Part of saouintw, the networking mod for the SAO UI

 * @author Bluexin
 */
@Suppress("unused")
internal class ClientProxy : CommonProxy() {

    override fun preInit() {
    }

    override fun getPlayerEntity(ctx: MessageContext): EntityPlayer? = if (ctx.side.isClient) Minecraft.getMinecraft().player else super.getPlayerEntity(ctx)

    override fun getMinecraftThread(ctx: MessageContext): IThreadListener = if (ctx.side.isClient) Minecraft.getMinecraft() else super.getMinecraftThread(ctx)

    override fun getMainWorld(): World = Minecraft.getMinecraft().world

    override fun getPlayerEntity(uuid: UUID): EntityPlayer? = FMLClientHandler.instance().worldClient?.getPlayerEntityByUUID(uuid)

    override fun getGameProfile(uuid: UUID) = FMLClientHandler.instance().client.connection?.getPlayerInfo(uuid)?.gameProfile

    override fun getPlayerHealth(uuid: UUID) = FMLClientHandler.instance().client.connection?.getPlayerInfo(uuid)?.lastHealth?.toFloat()?: {
        val player = EntityOtherPlayerMP(FMLClientHandler.instance().worldClient, getGameProfile(uuid)!!)
        FMLClientHandler.instance().worldClient.saveHandler.playerNBTManager.readPlayerData(player)?.getFloat("Health")?: 0f
    }.invoke()

    override fun getPlayerMaxHealth(uuid: UUID): Float {
        val player = EntityOtherPlayerMP(FMLClientHandler.instance().worldClient, getGameProfile(uuid)!!)
        return FMLClientHandler.instance().worldClient.saveHandler.playerNBTManager.readPlayerData(player)?.getTagList("Attributes", 10)?.getCompoundTagAt(0)?.getDouble("Base")?.toFloat()?: 0f
    }

    override fun getSide() = ProxySide.CLIENT
}
