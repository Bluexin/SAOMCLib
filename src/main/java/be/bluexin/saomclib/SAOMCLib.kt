package be.bluexin.saomclib

import be.bluexin.saomclib.capabilities.CapabilitiesHandler
import be.bluexin.saomclib.packets.PacketPipeline
import be.bluexin.saomclib.proxy.CommonProxy
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.common.SidedProxy
import net.minecraftforge.fml.common.event.FMLInitializationEvent
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent

/**
 * Main mod class.
 * Access instance in java trough [SAOMCLib.shenanigan] or [SAOMCLib.INSTANCE]
 *
 * @author Bluexin
 */
@Mod(modid = SAOMCLib.MODID, name = "SAOMC Library", version = "1.0")
object SAOMCLib {

    const val MODID = "saomclib"

    @Suppress("unused")
    @SidedProxy(clientSide = "be.bluexin.saomclib.proxy.ClientProxy", serverSide = "be.bluexin.saomclib.proxy.CommonProxy")
    internal lateinit var proxy: CommonProxy // Yeah I know people don' like hardcoded stuff. I don't care.

    @Mod.EventHandler
    fun preInit(e: FMLPreInitializationEvent) {
        MinecraftForge.EVENT_BUS.register(EventHandler())
//        MinecraftForge.EVENT_BUS.register(ExampleEventHandler())
//        PacketPipeline.registerMessage(SyncEntityCapabilityPacket::class.java, SyncEntityCapabilityPacket.Companion.Handler::class.java)
//        CapabilitiesHandler.registerEntityCapability(SimpleCapability::class.java, SimpleCapability.Companion.Storage(), { it is EntityPlayer })
//        CapabilitiesHandler.registerEntityCapability(JSimpleCapability::class.java, JSimpleCapability.Storage(), { it is EntityPlayer })
    }

    @Mod.EventHandler
    fun init(e: FMLInitializationEvent) {
        PacketPipeline.init()
        CapabilitiesHandler.setup()
    }

    @JvmStatic
    @Mod.InstanceFactory
    fun shenanigan() = this
}
