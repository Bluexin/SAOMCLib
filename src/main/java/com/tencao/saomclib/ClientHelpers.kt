package com.tencao.saomclib

import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiScreen

/**
 * Calls a profiled block, with given key.
 */
inline fun Minecraft.profile(key: String, body: () -> Unit) {
    this.mcProfiler.startSection(key)
    body()
    this.mcProfiler.endSection()
}

/**
 * From LibrarianLib, with edits mostly made by Tencao
 *
 * Using easy access methods to help and assist functions.
 */
object Client {
    val minecraft: Minecraft
        get() = Minecraft.getMinecraft()
    val resourceManager
        get() = minecraft.resourceManager
    val renderManager
        get() = minecraft.renderManager
    val textureManager
        get() = minecraft.textureManager
    val player
        get() = minecraft.player

    fun displayGuiScreen(screen: GuiScreen?) {
        minecraft.displayGuiScreen(screen)
    }
}
