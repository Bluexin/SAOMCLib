package be.bluexin.saomclib

import be.bluexin.saomclib.capabilities.CapabilitiesHandler
import be.bluexin.saomclib.capabilities.NoStorage
import be.bluexin.saomclib.capabilities.PartyCapability
import be.bluexin.saomclib.commands.PTCommand
import be.bluexin.saomclib.packets.PTS2CPacket
import be.bluexin.saomclib.packets.PacketPipeline
import be.bluexin.saomclib.packets.SyncEntityCapabilityPacket
import be.bluexin.saomclib.proxy.CommonProxy
import net.minecraft.entity.player.EntityPlayer
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.common.SidedProxy
import net.minecraftforge.fml.common.event.FMLInitializationEvent
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent
import net.minecraftforge.fml.common.event.FMLServerStartingEvent
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

/**
 * Main mod class.
 * Access instance in java trough [SAOMCLib.shenanigan] or [SAOMCLib.INSTANCE]
 *
 * @author Bluexin
 */
@Mod(modid = SAOMCLib.MODID, name = "SAOMC Library", version = SAOMCLib.VERSION, dependencies = SAOMCLib.DEPENDENCIES)
object SAOMCLib {

    const val MODID = "saomclib"
    const val VERSION = "1.2.0.4"
    const val DEPENDENCIES = "required-after:forgelin@[1.5.1,)"

    @Suppress("unused")
    @SidedProxy(clientSide = "be.bluexin.saomclib.proxy.ClientProxy", serverSide = "be.bluexin.saomclib.proxy.CommonProxy")
    internal lateinit var proxy: CommonProxy

    val LOGGER: Logger = LogManager.getLogger(MODID)

    @Mod.EventHandler
    fun preInit(e: FMLPreInitializationEvent) {
        MinecraftForge.EVENT_BUS.register(EventHandler())
        PacketPipeline.registerMessage(SyncEntityCapabilityPacket::class.java, SyncEntityCapabilityPacket.Companion.Handler::class.java)
        PacketPipeline.registerMessage(PTS2CPacket::class.java, PTS2CPacket.Companion.Handler::class.java)
        CapabilitiesHandler.registerEntityCapability(PartyCapability::class.java, NoStorage<PartyCapability>(), { it is EntityPlayer })
    }

    @Mod.EventHandler
    fun init(e: FMLInitializationEvent) {
        PacketPipeline.init()
        CapabilitiesHandler.setup()
    }

    @Mod.EventHandler
    fun serverStart(e: FMLServerStartingEvent) {
        e.registerServerCommand(PTCommand)
    }

    @JvmStatic
    @Mod.InstanceFactory
    fun shenanigan() = this
}
