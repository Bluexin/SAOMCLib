package be.bluexin.saomclib.proxy

import be.bluexin.saomclib.packets.PTC2SPacket
import be.bluexin.saomclib.packets.PTS2CPacket
import be.bluexin.saomclib.packets.PacketPipeline
import be.bluexin.saomclib.packets.SyncEntityCapabilityPacket
import net.minecraft.client.Minecraft
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.util.IThreadListener
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext

/**
 * Part of saouintw, the networking mod for the SAO UI

 * @author Bluexin
 */
@Suppress("unused")
internal class ClientProxy : CommonProxy() {

    override fun preInit(){}

    override fun getPlayerEntity(ctx: MessageContext): EntityPlayer? = if (ctx.side.isClient) Minecraft.getMinecraft().player else super.getPlayerEntity(ctx)

    override fun getMinecraftThread(ctx: MessageContext): IThreadListener = if (ctx.side.isClient) Minecraft.getMinecraft() else super.getMinecraftThread(ctx)
}
