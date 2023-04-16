package be.bluexin.saomclib.platform

import be.bluexin.saomclib.platform.services.PlatformHelper
import net.fabricmc.loader.api.FabricLoader

class FabricPlatformHelper : PlatformHelper {
    override val platformName = "Fabric"

    override fun isModLoaded(modId: String) = FabricLoader.getInstance().isModLoaded(modId)

    override val isDevelopmentEnvironment by lazy { FabricLoader.getInstance().isDevelopmentEnvironment }
}
