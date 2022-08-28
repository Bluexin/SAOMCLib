package com.tencao.saomclib.utils

import net.minecraft.client.Minecraft
import net.minecraft.client.gui.fonts.FontResourceManager
import net.minecraftforge.fml.common.ObfuscationReflectionHelper
import java.lang.reflect.Field

object ReflectHelper {

    val fontResourceMananger: Field = ObfuscationReflectionHelper.findField(Minecraft::class.java, "field_211501_aD")

    init {

        fontResourceMananger.isAccessible = true
    }
}

val Minecraft.fontResourceMananger: FontResourceManager
    get() = ReflectHelper.fontResourceMananger.get(this) as FontResourceManager
