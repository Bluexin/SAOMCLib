package com.tencao.saomclib

import net.minecraft.client.Minecraft
import net.minecraft.client.gui.screen.Screen

/**
 * Calls a profiled block, with given key.
 */
inline fun Minecraft.profile(key: String, body: () -> Unit) {
    this.profiler.startSection(key)
    body()
    this.profiler.endSection()
}

/**
 * From LibrarianLib, with edits mostly made by Tencao
 *
 * Using easy access methods to help and assist functions.
 */
object Client {
    val minecraft: Minecraft
        get() = Minecraft.getInstance()
    val resourceManager
        get() = minecraft.resourceManager
    val renderManager
        get() = minecraft.renderManager
    val textureManager
        get() = minecraft.textureManager
    val player
        get() = minecraft.player

    fun displayGuiScreen(screen: Screen?) {
        minecraft.displayGuiScreen(screen)
    }
}

val Minecraft.scaledWidth: Int
    get() = this.mainWindow.scaledWidth
val Minecraft.scaledHeight: Int
    get() = this.mainWindow.scaledHeight
