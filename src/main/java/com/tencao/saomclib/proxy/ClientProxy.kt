package com.tencao.saomclib.proxy

import net.minecraft.client.Minecraft
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.world.World
import java.util.*

/**
 * Part of saouintw, the networking mod for the SAO UI

 * @author Bluexin
 */
@Suppress("unused")
object ClientProxy : IProxy {

    // fun getPlayerEntity(ctx: MessageContext): PlayerEntity? = if (ctx.side.isClient) Minecraft.getInstance().player

    // override fun getMinecraftThread(ctx: MessageContext): IThreadListener = Minecraft.getInstance()

    override fun getMainWorld(): World = Minecraft.getInstance().level!!

    override fun getPlayerEntity(uuid: UUID): PlayerEntity? = Minecraft.getInstance().level?.getPlayerByUUID(uuid)

    override fun getGameProfile(uuid: UUID) = Minecraft.getInstance().connection?.getPlayerInfo(uuid)?.profile

    override fun getPlayerHealth(uuid: UUID): Float {
        return Minecraft.getInstance().connection?.getPlayerInfo(uuid)?.displayHealth?.toFloat() ?: 0f
    }

    override fun getPlayerMaxHealth(uuid: UUID): Float {
        return 20f
    }

    override fun isPlayerOnline(uuid: UUID) = (getGameProfile(uuid)?.name?.let { name ->
        Minecraft.getInstance().currentServer?.playerList?.any {
            it.string.contains(name, true)
        }
    } ?: Minecraft.getInstance().level?.getPlayerByUUID(uuid)) != null

    override var isServerSideLoaded: Boolean = false

    override val getSide = IProxy.ProxySide.CLIENT
}
