package com.tencao.saomclib

import com.tencao.saomclib.capabilities.BlockRecordCapability
import com.tencao.saomclib.capabilities.CapabilitiesHandler
import com.tencao.saomclib.capabilities.PartyCapability
import com.tencao.saomclib.commands.Command
import com.tencao.saomclib.events.BlockMonitor
import com.tencao.saomclib.events.EventHandler
import com.tencao.saomclib.events.FTBPartyEvents
import com.tencao.saomclib.events.TFPartyEvents
import com.tencao.saomclib.packets.MakeClientAwarePacket
import com.tencao.saomclib.packets.PacketPipeline
import com.tencao.saomclib.packets.SyncEntityCapabilityPacket
import com.tencao.saomclib.packets.party.PTUpdateClientPKT
import com.tencao.saomclib.packets.party.PTUpdateServerPKT
import com.tencao.saomclib.party.PartyManager
import com.tencao.saomclib.party.PlayerInfo
import com.tencao.saomclib.proxy.CommonProxy
import com.tencao.saomclib.utils.ModHelper
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
@Mod(
    modid = SAOMCLib.MODID,
    name = "SAOMC Library",
    version = SAOMCLib.VERSION,
    dependencies = SAOMCLib.DEPENDENCIES,
    modLanguageAdapter = "io.github.chaosunity.forgelin.KotlinAdapter"
)
object SAOMCLib {

    const val MODID = "saomclib"
    const val VERSION = "1.4.11"
    const val DEPENDENCIES = "required-after:forgelin_continuous@[1.5.30.0,)"

    @SidedProxy(clientSide = "com.tencao.saomclib.proxy.ClientProxy", serverSide = "com.tencao.saomclib.proxy.CommonProxy")
    lateinit var proxy: CommonProxy

    val LOGGER: Logger = LogManager.getLogger(MODID)

    @Mod.EventHandler
    fun preInit(e: FMLPreInitializationEvent) {
        MinecraftForge.EVENT_BUS.register(EventHandler)
        MinecraftForge.EVENT_BUS.register(BlockMonitor)
        if (ModHelper.isTogetherForeverLoaded) MinecraftForge.EVENT_BUS.register(TFPartyEvents)
        if (ModHelper.isFTBLibLoaded) MinecraftForge.EVENT_BUS.register(FTBPartyEvents)
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
        if (ModHelper.isTogetherForeverLoaded) {
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
