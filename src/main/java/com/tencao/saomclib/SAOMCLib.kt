package com.tencao.saomclib

import com.tencao.saomclib.SAOMCLib.MODID
import com.tencao.saomclib.capabilities.BlockRecordCapability
import com.tencao.saomclib.capabilities.CapabilitiesHandler
import com.tencao.saomclib.capabilities.PartyCapability
import com.tencao.saomclib.events.BlockListener
import com.tencao.saomclib.events.ClientEventListener
import com.tencao.saomclib.events.EventHandler
import com.tencao.saomclib.packets.PacketPipeline
import com.tencao.saomclib.packets.toClient.MakeClientAwarePacket
import com.tencao.saomclib.packets.toClient.PTUpdateClientPKT
import com.tencao.saomclib.packets.toClient.SyncEntityCapabilityPacket
import com.tencao.saomclib.packets.toServer.PTUpdateServerPKT
import com.tencao.saomclib.proxy.ClientProxy
import com.tencao.saomclib.proxy.ServerProxy
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.util.ResourceLocation
import net.minecraft.world.chunk.Chunk
import net.minecraftforge.event.RegisterCommandsEvent
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.fml.DistExecutor
import net.minecraftforge.fml.DistExecutor.SafeSupplier
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent
import org.apache.logging.log4j.LogManager
import thedarkcolour.kotlinforforge.forge.FORGE_BUS
import thedarkcolour.kotlinforforge.forge.MOD_BUS

@Mod(MODID)
object SAOMCLib {

    const val MODID = "saomclib"
    val LOGGER = LogManager.getLogger(MODID)
    val proxy = DistExecutor.safeRunForDist({ SafeSupplier { ClientProxy } }) { SafeSupplier { ServerProxy } }

    fun rl(path: String): ResourceLocation {
        return ResourceLocation(MODID, path)
    }

    init {
        FORGE_BUS.register(this)
        MOD_BUS.addListener(::setup)
        MOD_BUS.addListener(::enqueueIMC)
        MOD_BUS.addListener(::clientSetup)
        FORGE_BUS.addListener(::registerCommands)
        // eventBus.register(this)
    }

    @SubscribeEvent
    fun setup(event: FMLCommonSetupEvent) {
        FORGE_BUS.register(EventHandler)
        FORGE_BUS.register(BlockListener)
        CapabilitiesHandler.registerChunkCapability(BlockRecordCapability::class.java, BlockRecordCapability.Storage()) { it is Chunk }
        CapabilitiesHandler.registerEntityCapability(PartyCapability::class.java, PartyCapability.PartyStorage) { it is PlayerEntity }
        PacketPipeline.registerServerToClientMessage(PTUpdateClientPKT::class.java, PTUpdateClientPKT.Companion::decode)
        PacketPipeline.registerClientToServerMessage(PTUpdateServerPKT::class.java, PTUpdateServerPKT.Companion::decode)
        PacketPipeline.registerServerToClientMessage(SyncEntityCapabilityPacket::class.java, SyncEntityCapabilityPacket.Companion::decode)
        PacketPipeline.registerServerToClientMessage(MakeClientAwarePacket::class.java, MakeClientAwarePacket.Companion::decode)
    }

    @SubscribeEvent
    fun clientSetup(event: FMLClientSetupEvent) {
        FORGE_BUS.register(ClientEventListener)
    }

    @SubscribeEvent
    fun enqueueIMC(event: InterModEnqueueEvent) {
        CapabilitiesHandler.setup()
    }

    @SubscribeEvent
    fun registerCommands(event: RegisterCommandsEvent) {
        /*
        val root: LiteralArgumentBuilder<CommandSource> = Commands.literal("saomc")
            .then(Commands.literal("pt")
                .then(PTCommands.INVITE.register())
                .then(PTCommands.ACCEPT.register())
                .then(PTCommands.CANCEL.register())
                .then(PTCommands.DECLINE.register())
                .then(PTCommands.KICK.register())
                .then(PTCommands.LEAVE.register())
                .then(PTCommands.PRINT.register())
            )
        event.dispatcher.register(root)*/
    }
}
