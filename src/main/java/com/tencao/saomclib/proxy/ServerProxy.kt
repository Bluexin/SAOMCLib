package com.tencao.saomclib.proxy

import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.ServerPlayerEntity
import net.minecraft.server.MinecraftServer
import net.minecraft.world.World
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.common.util.FakePlayer
import net.minecraftforge.fml.loading.FMLEnvironment
import net.minecraftforge.fml.server.ServerLifecycleHooks
import java.util.*

/**
 * Part of saouintw, the networking mod for the SAO UI

 * @author Bluexin
 */
object ServerProxy : IProxy {

    //override fun getPlayerEntity(ctx: MessageContext): PlayerEntity = ctx.serverHandler.player

    //override fun getMinecraftThread(ctx: MessageContext): IThreadListener = ServerLifecycleHooks.getCurrentServer()

    override fun getMainWorld(): World = ServerLifecycleHooks.getCurrentServer().worlds.first()

    override fun getPlayerEntity(uuid: UUID): PlayerEntity? = ServerLifecycleHooks.getCurrentServer().playerList.getPlayerByUUID(uuid)

    override fun getGameProfile(uuid: UUID) = ServerLifecycleHooks.getCurrentServer().playerProfileCache.getProfileByUUID(uuid)

    override fun getPlayerHealth(uuid: UUID): Float {
        return ServerLifecycleHooks.getCurrentServer().playerList.getPlayerByUUID(uuid)?.health?: 0f
    }

    override fun getPlayerMaxHealth(uuid: UUID): Float {
        return ServerLifecycleHooks.getCurrentServer().playerList.getPlayerByUUID(uuid)?.maxHealth?: 0f
    }

    override fun isPlayerOnline(uuid: UUID) = getPlayerEntity(uuid) != null

    override val getSide = IProxy.ProxySide.SERVER

    override var isServerSideLoaded: Boolean = true

}
