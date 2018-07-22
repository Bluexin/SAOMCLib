package be.bluexin.saomclib.commands

import net.minecraft.command.CommandBase
import net.minecraft.command.ICommandSender
import net.minecraft.command.WrongUsageException
import net.minecraft.entity.player.EntityPlayer

// TODO: improve registering and handling of commands
object CommandBase : CommandBase() {
    override fun getCommandName() = "saomc"
    override fun getCommandUsage(sender: ICommandSender?) = "commands.saomc.usage"

    override fun getRequiredPermissionLevel() = 0

    override fun canCommandSenderUseCommand(sender: ICommandSender) = sender is EntityPlayer

    override fun processCommand(sender: ICommandSender, args: Array<out String>) {
        if (args.isEmpty()) throw WrongUsageException(PTCommand.getCommandUsage(sender))
        if (sender !is EntityPlayer) throw WrongUsageException("commands.pt.playeronly")
        when (args[0]) {
            "pt" -> PTCommand.processCommand(sender, args.copyOfRange(1, args.size))
            "sync" -> SyncCMD.processCommand(sender, args)
        }
    }

    override fun addTabCompletionOptions(sender: ICommandSender, args: Array<out String>): MutableList<String> {
        if (sender !is EntityPlayer) return mutableListOf()
        if (args.isEmpty()) throw WrongUsageException(getCommandUsage(sender))
        return when (args.size) {
            1 -> {
                val l = mutableListOf("pt")
                @Suppress("UNCHECKED_CAST")
                CommandBase.getListOfStringsMatchingLastWord(args, *l.toTypedArray()) as MutableList<String>
            }
            else -> {
                val l = mutableListOf<String>()
                when (args[0]) {
                    "pt" -> return PTCommand.addTabCompletionOptions(sender, args.copyOfRange(1, args.size))
                }
                @Suppress("UNCHECKED_CAST")
                CommandBase.getListOfStringsMatchingLastWord(args, *l.toTypedArray()) as MutableList<String>
            }
        }
    }
}