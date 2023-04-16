package com.tencao.saomclib.commands

import net.minecraft.command.CommandBase
import net.minecraft.command.ICommandSender
import net.minecraft.command.WrongUsageException
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.server.MinecraftServer
import net.minecraft.util.math.BlockPos
import net.minecraft.util.text.ITextComponent
import net.minecraft.util.text.Style
import net.minecraft.util.text.TextFormatting

object Command : CommandBase() {
    override fun getName() = "saomc"
    override fun getUsage(sender: ICommandSender?) = "commands.saomc.usage"
    override fun getRequiredPermissionLevel() = 0
    override fun checkPermission(server: MinecraftServer?, sender: ICommandSender) = sender is EntityPlayer

    override fun execute(server: MinecraftServer, sender: ICommandSender, params: Array<String>) {
        if (params.isEmpty()) throw WrongUsageException(getUsage(sender))
        if (sender !is EntityPlayer) throw WrongUsageException("commands.pt.playeronly")

        CommandList.values().firstOrNull { it.getID().equals(params[0], true) }
            ?.execute(server, sender, params.drop(1).toTypedArray())
            ?: throw WrongUsageException(getUsage(sender))
    }

    override fun getTabCompletions(server: MinecraftServer, sender: ICommandSender, params: Array<String>, targetPos: BlockPos?): MutableList<String> {
        val commands = CommandList.values().filter { it.checkPermission(server, sender) }
        val command = commands.firstOrNull { command -> command.getID().equals(params[0], true) }
            ?: return getListOfStringsMatchingLastWord(params, commands.map { it.getID() })
        return if (params.size > 1) command.getTabCompletions(server, sender, params.drop(1).toTypedArray(), targetPos)
        else mutableListOf()
    }

    fun sendSuccess(sender: ICommandSender, message: ITextComponent) {
        sendMessage(sender, message.setStyle(Style().setParentStyle(message.style).setColor(TextFormatting.GREEN)))
    }

    fun sendError(sender: ICommandSender, message: ITextComponent) {
        sendMessage(sender, message.setStyle(Style().setParentStyle(message.style).setColor(TextFormatting.RED)))
    }

    fun sendMessage(sender: ICommandSender, message: ITextComponent) {
        sender.sendMessage(message)
    }
}
