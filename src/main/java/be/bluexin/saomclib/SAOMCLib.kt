package be.bluexin.saomclib

import be.bluexin.saomclib.capabilities.CapabilitiesHandler
import be.bluexin.saomclib.capabilities.CapabilityManager
import be.bluexin.saomclib.capabilities.PartyCapability
import be.bluexin.saomclib.commands.CommandBase
import be.bluexin.saomclib.events.EventHandler
import be.bluexin.saomclib.packets.PTC2SPacket
import be.bluexin.saomclib.packets.PTS2CPacket
import be.bluexin.saomclib.packets.PacketPipeline
import be.bluexin.saomclib.packets.SyncEntityCapabilityPacket
import be.bluexin.saomclib.proxy.CommonProxy
import cpw.mods.fml.common.FMLCommonHandler
import cpw.mods.fml.common.Mod
import cpw.mods.fml.common.SidedProxy
import cpw.mods.fml.common.event.FMLInitializationEvent
import cpw.mods.fml.common.event.FMLPreInitializationEvent
import cpw.mods.fml.common.event.FMLServerStartingEvent
import net.minecraft.entity.player.EntityPlayer
import net.minecraftforge.common.MinecraftForge
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

/**
 * Main mod class.
 * Access instance in java trough [SAOMCLib.shenanigan] or [SAOMCLib.INSTANCE]
 *
 * @author Bluexin
 */
@Suppress("KDocUnresolvedReference")
@Mod(modid = SAOMCLib.MODID, name = "SAOMC Library", version = SAOMCLib.VERSION, dependencies = SAOMCLib.DEPENDENCIES)
object SAOMCLib {

    const val MODID = "saomclib"
    const val VERSION = "1.2.1.0"
    const val DEPENDENCIES = ""

    @Suppress("unused")
    @SidedProxy(clientSide = "be.bluexin.saomclib.proxy.ClientProxy", serverSide = "be.bluexin.saomclib.proxy.CommonProxy")
    internal lateinit var proxy: CommonProxy

    val LOGGER: Logger = LogManager.getLogger(MODID)

    @Mod.EventHandler
    fun preInit(e: FMLPreInitializationEvent) {
        CapabilityManager.injectCapabilities(e.asmData)
        MinecraftForge.EVENT_BUS.register(EventHandler)
        FMLCommonHandler.instance().bus().register(EventHandler)
        CapabilitiesHandler.registerEntityCapability(PartyCapability::class.java, PartyCapability.PartyStorage) { it is EntityPlayer }
        PacketPipeline.registerMessage(PTC2SPacket::class.java, PTC2SPacket.Companion.Handler::class.java)
        PacketPipeline.registerMessage(PTS2CPacket::class.java, PTS2CPacket.Companion.Handler::class.java)
        PacketPipeline.registerMessage(SyncEntityCapabilityPacket::class.java, SyncEntityCapabilityPacket.Companion.Handler::class.java)
    }

    @Mod.EventHandler
    fun init(e: FMLInitializationEvent) {
        PacketPipeline.init()
        CapabilitiesHandler.setup()
    }

    @Mod.EventHandler
    fun serverStart(e: FMLServerStartingEvent) {
        e.registerServerCommand(CommandBase)
    }

    @JvmStatic
    @Mod.InstanceFactory
    fun shenanigan() = this
}
