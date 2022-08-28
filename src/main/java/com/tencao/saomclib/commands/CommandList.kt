package com.tencao.saomclib.commands

import net.minecraft.command.ICommandSender
import net.minecraft.command.WrongUsageException
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.server.MinecraftServer
import net.minecraft.util.math.BlockPos
import net.minecraft.util.text.TextComponentTranslation

enum class CommandList : CommandBase {
    PT {
        override fun getTabCompletions(server: MinecraftServer, sender: ICommandSender, params: Array<String>, pos: BlockPos?): MutableList<String> {
            val commands = PTCommands.values().filter { it.checkPermission(server, sender) }
            val command = commands.firstOrNull { command -> command.getID().equals(params[0], true) }
                ?: return getListOfStringsMatchingLastWord(params, commands.map { it.getID() })
            return if (params.size > 1) command.getTabCompletions(server, sender, params.drop(1).toTypedArray(), pos)
            else mutableListOf()
        }

        override fun execute(server: MinecraftServer, sender: ICommandSender, params: Array<String>) {
            if (params.isEmpty()) throw WrongUsageException("commands.pt.usage")
            if (sender !is EntityPlayer) throw WrongUsageException("commands.pt.playeronly")

            PTCommands.values().firstOrNull { it.getID().equals(params[0], true) }
                ?.execute(server, sender, params.drop(1).toTypedArray())
                ?: throw WrongUsageException("commands.pt.usage")
        }
    };

    override fun getID(): String {
        return TextComponentTranslation("commands.${name.toLowerCase()}").unformattedText
    }

    override fun getUsage(sender: ICommandSender): String {
        return "commands.${name.toLowerCase()}.usage"
    }

    companion object {
        val commands = values().map { it.getID() }
    }
}
