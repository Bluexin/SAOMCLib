package be.bluexin.saomclib.utils

import net.minecraftforge.fml.common.Loader

object ModHelper {

    val isTogetherForeverLoaded by lazy { Loader.isModLoaded("togetherforever") }
}