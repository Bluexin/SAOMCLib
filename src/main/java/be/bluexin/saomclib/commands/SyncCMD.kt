package be.bluexin.saomclib.commands

import be.bluexin.saomclib.SAOMCLib
import be.bluexin.saomclib.capabilities.CapabilitiesHandler
import net.minecraft.command.CommandBase
import net.minecraft.command.ICommandSender
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.server.MinecraftServer
import net.minecraft.util.math.BlockPos

// TODO: remove
object SyncCMD : CommandBase() {
    override fun getName() = "sync"
    override fun getUsage(sender: ICommandSender?) = "commands.sync.usage"
    override fun getRequiredPermissionLevel() = 0
    override fun checkPermission(server: MinecraftServer?, sender: ICommandSender) = sender is EntityPlayer

    override fun execute(server: MinecraftServer, sender: ICommandSender, args: Array<out String>) {
        SAOMCLib.LOGGER.info("Sending sync packet to ${sender.name}")
        CapabilitiesHandler.syncEntitiesLogin(sender.commandSenderEntity as EntityPlayer)
    }

    override fun getTabCompletions(server: MinecraftServer, sender: ICommandSender, args: Array<out String>, pos: BlockPos?): MutableList<String> {
        return mutableListOf()
    }
}