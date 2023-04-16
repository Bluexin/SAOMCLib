package be.bluexin.saomclib.platform

import be.bluexin.saomclib.platform.services.PlatformHelper
import net.minecraftforge.fml.ModList
import net.minecraftforge.fml.loading.FMLLoader

class ForgePlatformHelper : PlatformHelper {
    override val platformName = "Forge"

    override fun isModLoaded(modId: String) = ModList.get().isLoaded(modId)

    override val isDevelopmentEnvironment by lazy { !FMLLoader.isProduction() }
}