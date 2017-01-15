package be.bluexin.saomclib

import be.bluexin.saomclib.capabilities.CapabilitiesHandler
import be.bluexin.saomclib.example.SimpleCapability
import be.bluexin.saomclib.packets.PacketPipeline
import be.bluexin.saomclib.packets.SyncEntityCapabilityPacket
import be.bluexin.saomclib.proxy.CommonProxy
import net.minecraft.entity.player.EntityPlayer
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.common.SidedProxy
import net.minecraftforge.fml.common.event.FMLInitializationEvent
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent
import net.minecraftforge.fml.common.network.NetworkCheckHandler
import net.minecraftforge.fml.relauncher.Side

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
        PacketPipeline.registerMessage(SyncEntityCapabilityPacket::class.java, SyncEntityCapabilityPacket.Companion.Handler::class.java)
        CapabilitiesHandler.registerEntityCapability(SimpleCapability::class.java, SimpleCapability.Companion.Storage(), { it is EntityPlayer })
    }

    @Mod.EventHandler
    fun init(e: FMLInitializationEvent) {
        PacketPipeline.init()
        CapabilitiesHandler.setup()
    }

    @NetworkCheckHandler
    fun checkOtherSide(remoteVersions: Map<String, String>, side: Side): Boolean {
        //        if (side.equals(Side.SERVER)) {
        //            Communicator.INSTANCE.setSupportsPackets(SAOCore.VERSION.equals(remoteVersions.get(SAOCore.MODID + "ntw")));
        //            LogHelper.logInfo("Connected to a server " + (Communicator.INSTANCE.getSupportsPackets() ? "with" : "without") + " support for Packets.");
        //        }

        return true
    }

    @JvmStatic
    @Mod.InstanceFactory
    fun shenanigan() = this
}
