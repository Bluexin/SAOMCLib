package be.bluexin.saomclib

import be.bluexin.saomclib.capabilities.BlockRecordCapability
import be.bluexin.saomclib.capabilities.CapabilitiesHandler
import be.bluexin.saomclib.capabilities.PartyCapability
import be.bluexin.saomclib.commands.Command
import be.bluexin.saomclib.events.BlockMonitor
import be.bluexin.saomclib.events.EventHandler
import be.bluexin.saomclib.events.TFPartyEvents
import be.bluexin.saomclib.packets.MakeClientAwarePacket
import be.bluexin.saomclib.packets.PacketPipeline
import be.bluexin.saomclib.packets.SyncEntityCapabilityPacket
import be.bluexin.saomclib.packets.party.PTUpdateClientPKT
import be.bluexin.saomclib.packets.party.PTUpdateServerPKT
import be.bluexin.saomclib.party.PartyManager
import be.bluexin.saomclib.party.PlayerInfo
import be.bluexin.saomclib.proxy.CommonProxy
import be.bluexin.saomclib.utils.ModHelper
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.world.chunk.Chunk
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
@Suppress("KDocUnresolvedReference")
@Mod(modid = SAOMCLib.MODID, name = "SAOMC Library", version = SAOMCLib.VERSION, dependencies = SAOMCLib.DEPENDENCIES)
object SAOMCLib {

    const val MODID = "saomclib"
    const val VERSION = "1.4.9"
    const val DEPENDENCIES = "required-after:forgelin@[1.8.4,)"

    @SidedProxy(clientSide = "be.bluexin.saomclib.proxy.ClientProxy", serverSide = "be.bluexin.saomclib.proxy.CommonProxy")
    lateinit var proxy: CommonProxy

    val LOGGER: Logger = LogManager.getLogger(MODID)

    @Mod.EventHandler
    fun preInit(e: FMLPreInitializationEvent) {
        MinecraftForge.EVENT_BUS.register(EventHandler)
        MinecraftForge.EVENT_BUS.register(BlockMonitor)
        if (ModHelper.isTogetherForeverLoaded) MinecraftForge.EVENT_BUS.register(TFPartyEvents)
        proxy.preInit()
        CapabilitiesHandler.registerChunkCapability(BlockRecordCapability::class.java, BlockRecordCapability.Storage()) { it is Chunk }
        CapabilitiesHandler.registerEntityCapability(PartyCapability::class.java, PartyCapability.PartyStorage) { it is EntityPlayer }
        PacketPipeline.registerMessage(PTUpdateClientPKT::class.java, PTUpdateClientPKT.Companion.Handler::class.java)
        PacketPipeline.registerMessage(PTUpdateServerPKT::class.java, PTUpdateServerPKT.Companion.Handler::class.java)
        PacketPipeline.registerMessage(SyncEntityCapabilityPacket::class.java, SyncEntityCapabilityPacket.Companion.Handler::class.java)
        PacketPipeline.registerMessage(MakeClientAwarePacket::class.java, MakeClientAwarePacket.Companion.Handler::class.java)
    }

    @Mod.EventHandler
    fun init(e: FMLInitializationEvent) {
        PacketPipeline.init()
        CapabilitiesHandler.setup()
    }

    @Mod.EventHandler
    fun serverStart(e: FMLServerStartingEvent) {
        e.registerServerCommand(Command)
        PartyManager.clean()
        if (ModHelper.isTogetherForeverLoaded){
            com.buuz135.togetherforever.api.TogetherForeverAPI.getInstance().teams.forEach { tfParty ->
                val party = PartyManager.createParty(PlayerInfo(tfParty.owner))
                tfParty.players.forEach { party.addMember(it.uuid) }
            }
        }
    }

    @JvmStatic
    @Mod.InstanceFactory
    fun shenanigan() = this
}
