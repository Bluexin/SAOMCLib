package com.tencao.saomclib

import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.tencao.saomclib.SAOMCLib.MODID
import com.tencao.saomclib.capabilities.BlockRecordCapability
import com.tencao.saomclib.capabilities.CapabilitiesHandler
import com.tencao.saomclib.capabilities.PartyCapability
import com.tencao.saomclib.commands.PTCommands
import com.tencao.saomclib.events.BlockListener
import com.tencao.saomclib.events.EventHandler
import com.tencao.saomclib.packets.PacketPipeline
import com.tencao.saomclib.packets.to_client.MakeClientAwarePacket
import com.tencao.saomclib.packets.to_client.PTUpdateClientPKT
import com.tencao.saomclib.packets.to_client.SyncEntityCapabilityPacket
import com.tencao.saomclib.packets.to_server.PTUpdateServerPKT
import com.tencao.saomclib.proxy.ClientProxy
import com.tencao.saomclib.proxy.ServerProxy
import net.minecraft.command.CommandSource
import net.minecraft.command.Commands
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.util.ResourceLocation
import net.minecraft.world.chunk.Chunk
import net.minecraftforge.common.MinecraftForge
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
        MOD_BUS.addListener(::setup)
        MOD_BUS.addListener(::enqueueIMC)
        FORGE_BUS.addListener(::registerCommands)
        //eventBus.register(this)
        MinecraftForge.EVENT_BUS.register(this)
    }

    @SubscribeEvent
    @JvmSynthetic
    fun setup(event: FMLCommonSetupEvent) {
        MinecraftForge.EVENT_BUS.register(EventHandler)
        MinecraftForge.EVENT_BUS.register(BlockListener)
        CapabilitiesHandler.registerChunkCapability(BlockRecordCapability::class.java, BlockRecordCapability.Storage()) { it is Chunk }
        CapabilitiesHandler.registerEntityCapability(PartyCapability::class.java, PartyCapability.PartyStorage) { it is PlayerEntity }
        PacketPipeline.registerServerToClientMessage(PTUpdateClientPKT::class.java, PTUpdateClientPKT.Companion::decode)
        PacketPipeline.registerClientToServerMessage(PTUpdateServerPKT::class.java, PTUpdateServerPKT.Companion::decode)
        PacketPipeline.registerServerToClientMessage(SyncEntityCapabilityPacket::class.java, SyncEntityCapabilityPacket.Companion::decode)
        PacketPipeline.registerServerToClientMessage(MakeClientAwarePacket::class.java, MakeClientAwarePacket.Companion::decode)
    }

    @SubscribeEvent
    @JvmSynthetic
    fun enqueueIMC(event: InterModEnqueueEvent) {
        CapabilitiesHandler.setup()
    }

    @SubscribeEvent
    @JvmSynthetic
    fun registerCommands(event: RegisterCommandsEvent) {
        val commands = PTCommands.values()
        var index = 0
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
        event.dispatcher.register(root)
    }

}