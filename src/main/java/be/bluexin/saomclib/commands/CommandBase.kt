package be.bluexin.saomclib.commands

import be.bluexin.saomclib.SAOMCLib
import net.minecraft.command.CommandBase
import net.minecraft.command.ICommandSender
import net.minecraft.command.WrongUsageException
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.server.MinecraftServer
import net.minecraft.util.math.BlockPos

object CommandBase : CommandBase() {

    override fun getName() = "saomc"
    override fun getUsage(sender: ICommandSender?) = "commands.saomc.usage"

    override fun getRequiredPermissionLevel() = 0

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
    }
}