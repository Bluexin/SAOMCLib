package com.tencao.saomclib.proxy

import net.minecraft.entity.player.PlayerEntity
import net.minecraft.world.World
import net.minecraftforge.fml.server.ServerLifecycleHooks
import java.util.*

/**
 * Part of saouintw, the networking mod for the SAO UI

 * @author Bluexin
 */
object ServerProxy : IProxy {

    // override fun getPlayerEntity(ctx: MessageContext): PlayerEntity = ctx.serverHandler.player

    // override fun getMinecraftThread(ctx: MessageContext): IThreadListener = ServerLifecycleHooks.getCurrentServer()

    override fun getMainWorld(): World = ServerLifecycleHooks.getCurrentServer().allLevels.first()

    override fun getPlayerEntity(uuid: UUID): PlayerEntity? = ServerLifecycleHooks.getCurrentServer().playerList.getPlayer(uuid)

    override fun getGameProfile(uuid: UUID) = ServerLifecycleHooks.getCurrentServer().profileCache.get(uuid)

    override fun getPlayerHealth(uuid: UUID): Float {
        return ServerLifecycleHooks.getCurrentServer().playerList.getPlayer(uuid)?.health ?: 0f
    }

    override fun getPlayerMaxHealth(uuid: UUID): Float {
        return ServerLifecycleHooks.getCurrentServer().playerList.getPlayer(uuid)?.maxHealth ?: 0f
    }

    override fun isPlayerOnline(uuid: UUID) = getPlayerEntity(uuid) != null

    override val getSide = IProxy.ProxySide.SERVER

    override var isServerSideLoaded: Boolean = true
}
