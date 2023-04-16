package be.bluexin.saomclib

import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import thedarkcolour.kotlinforforge.forge.MOD_BUS

/**
 * Main mod class.
 *
 * @author Bluexin
 */
@Mod(Constants.MOD_ID)
object SAOMCLib {

    @Deprecated(
        message = "use Constants",
        ReplaceWith("Constants.MOD_ID", "be.bluexin.saomclib.Constants"),
        level = DeprecationLevel.ERROR
    )
    const val MODID = "saomclib"

//    @SidedProxy(clientSide = "com.tencao.saomclib.proxy.ClientProxy", serverSide = "com.tencao.saomclib.proxy.CommonProxy")
//    lateinit var proxy: CommonProxy

    @Deprecated(
        message = "use Constants",
        ReplaceWith("Constants.LOG", "be.bluexin.saomclib.Constants"),
        level = DeprecationLevel.ERROR
    )
    val LOGGER: Logger = LogManager.getLogger(Constants.MOD_ID)

    init {
        MOD_BUS.addListener(::setup)
    }

    private fun setup(e: FMLCommonSetupEvent) {
//        MinecraftForge.EVENT_BUS.register(EventHandler)
//        MinecraftForge.EVENT_BUS.register(BlockMonitor)
//        if (ModHelper.isTogetherForeverLoaded) MinecraftForge.EVENT_BUS.register(TFPartyEvents)
//        if (ModHelper.isFTBLibLoaded) MinecraftForge.EVENT_BUS.register(FTBPartyEvents)
//        proxy.preInit()
//        CapabilitiesHandler.registerChunkCapability(BlockRecordCapability::class.java, BlockRecordCapability.Storage()) { it is Chunk }
//        CapabilitiesHandler.registerEntityCapability(PartyCapability::class.java, PartyCapability.PartyStorage) { it is EntityPlayer }
//        PacketPipeline.registerMessage(PTUpdateClientPKT::class.java, PTUpdateClientPKT.Companion.Handler::class.java)
//        PacketPipeline.registerMessage(PTUpdateServerPKT::class.java, PTUpdateServerPKT.Companion.Handler::class.java)
//        PacketPipeline.registerMessage(SyncEntityCapabilityPacket::class.java, SyncEntityCapabilityPacket.Companion.Handler::class.java)
//        PacketPipeline.registerMessage(MakeClientAwarePacket::class.java, MakeClientAwarePacket.Companion.Handler::class.java)
//        PacketPipeline.init()
//        CapabilitiesHandler.setup()
//        e.registerServerCommand(Command)
//        PartyManager.clean()
//        if (ModHelper.isTogetherForeverLoaded) {
//            com.buuz135.togetherforever.api.TogetherForeverAPI.getInstance().teams.forEach { tfParty ->
//                val party = PartyManager.createParty(PlayerInfo(tfParty.owner))
//                tfParty.players.forEach { party.addMember(it.uuid) }
//            }
//        }
        CommonClass.init()
    }
}
