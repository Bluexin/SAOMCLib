package be.bluexin.saomclib.commands

import net.minecraft.command.CommandBase
import net.minecraft.command.CommandException
import net.minecraft.command.ICommandSender
import net.minecraft.server.MinecraftServer
import net.minecraft.util.math.BlockPos
import net.minecraft.util.text.ITextComponent
import net.minecraft.util.text.Style
import net.minecraft.util.text.TextFormatting

interface CommandBase {

    /**
     * Gets the name of the command
     */
    fun getID(): String

    fun getRequiredPermissionLevel(): Int {
        return 0
    }

    fun getUsage(sender: ICommandSender): String


    fun getTabCompletions(server: MinecraftServer, sender: ICommandSender, params: Array<String>, pos: BlockPos?): MutableList<String> {
        return mutableListOf()
    }

    /**
     * Check if the given ICommandSender has permission to execute this command
     */
    fun checkPermission(server: MinecraftServer, sender: ICommandSender): Boolean {
        return sender.canUseCommand(this.getRequiredPermissionLevel(), this.getID())
    }

    @Throws(CommandException::class)
    fun execute(server: MinecraftServer, sender: ICommandSender, params: Array<String>)

    fun sendSuccess(sender: ICommandSender, message: ITextComponent) {
        sendMessage(sender, message.setStyle(Style().setParentStyle(message.style).setColor(TextFormatting.GREEN)))
    }

    fun sendError(sender: ICommandSender, message: ITextComponent) {
        sendMessage(sender, message.setStyle(Style().setParentStyle(message.style).setColor(TextFormatting.RED)))
    }

    fun sendMessage(sender: ICommandSender, message: ITextComponent) {
        sender.sendMessage(message)
    }

    /**
     * Returns a List of strings (chosen from the given strings) which the last word in the given string array is a
     * beginning-match for. (Tab completion).
     */
    fun getListOfStringsMatchingLastWord(args: Array<String>, possibilities: Collection<String>): MutableList<String> {
        return CommandBase.getListOfStringsMatchingLastWord(args, possibilities)
    }
    /*

    override fun checkPermission(server: MinecraftServer?, sender: ICommandSender) = sender is EntityPlayer

    override fun execute(server: MinecraftServer, sender: ICommandSender, args: Array<out String>) {
        if (args.isEmpty()) throw WrongUsageException(PTCommand.getUsage(sender))
        if (sender !is EntityPlayer) throw WrongUsageException("commands.pt.playeronly")
        when (args[0]) {
            "pt" -> PTCommand.execute(server, sender, args.copyOfRange(1, args.size))
            "sync" -> SyncCMD.execute(server, sender, args)
        }
    }

    override fun getTabCompletions(server: MinecraftServer, sender: ICommandSender, args: Array<out String>, pos: BlockPos?): MutableList<String> {
        if (sender !is EntityPlayer) return mutableListOf()
        if (args.isEmpty()) throw WrongUsageException(getUsage(sender))
        return when (args.size) {
            1 -> {
                val l = mutableListOf("pt")
                CommandBase.getListOfStringsMatchingLastWord(args, *l.toTypedArray())
            }
            else -> {
                val l = mutableListOf<String>()
                when (args[0]) {
                    "pt" -> return PTCommand.getTabCompletions(server, sender, args.copyOfRange(1, args.size), pos)
                }
                CommandBase.getListOfStringsMatchingLastWord(args, *l.toTypedArray())
            }
        }
    }*/
}