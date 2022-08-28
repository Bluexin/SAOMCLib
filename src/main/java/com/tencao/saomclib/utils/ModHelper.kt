package com.tencao.saomclib.utils

import net.minecraftforge.fml.common.Loader

object ModHelper {

    val isTogetherForeverLoaded by lazy { Loader.isModLoaded("togetherforever") }

    val isFTBLibLoaded by lazy { Loader.isModLoaded("ftblib") }
}
