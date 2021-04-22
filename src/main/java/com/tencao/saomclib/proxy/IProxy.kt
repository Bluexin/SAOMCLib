package com.tencao.saomclib.proxy

import com.mojang.authlib.GameProfile
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.world.World
import java.util.*

interface IProxy {

    //fun getPlayerEntity(ctx: MessageContext): PlayerEntity

    //fun getMinecraftThread(ctx: MessageContext): IThreadListener

    fun getMainWorld(): World

    fun getPlayerEntity(uuid: UUID): PlayerEntity?

    fun getGameProfile(uuid: UUID): GameProfile?

    fun getPlayerHealth(uuid: UUID): Float

    fun getPlayerMaxHealth(uuid: UUID): Float

    fun isPlayerOnline(uuid: UUID): Boolean

    var isServerSideLoaded: Boolean

    val getSide: ProxySide

    enum class ProxySide {
        CLIENT,
        SERVER;
    }
}