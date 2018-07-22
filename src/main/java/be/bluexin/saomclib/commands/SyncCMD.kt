package be.bluexin.saomclib.commands

import be.bluexin.saomclib.SAOMCLib
import be.bluexin.saomclib.capabilities.CapabilitiesHandler
import net.minecraft.command.CommandBase
import net.minecraft.command.ICommandSender
import net.minecraft.entity.player.EntityPlayer

// TODO: remove
object SyncCMD : CommandBase() {

    override fun getCommandName() = "sync"
    override fun getCommandUsage(sender: ICommandSender?) = "commands.sync.usage"
    override fun getRequiredPermissionLevel() = 0
    override fun canCommandSenderUseCommand(sender: ICommandSender) = sender is EntityPlayer

    override fun processCommand(sender: ICommandSender, args: Array<out String>) {
        SAOMCLib.LOGGER.info("Sending sync packet to ${sender.commandSenderName}")
        if (sender is EntityPlayer) CapabilitiesHandler.syncEntitiesLogin(sender)
    }

    override fun addTabCompletionOptions(sender: ICommandSender, args: Array<out String>): MutableList<String> {
        return mutableListOf()
    }
}