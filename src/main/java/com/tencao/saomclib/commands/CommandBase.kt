package com.tencao.saomclib.commands

import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import net.minecraft.command.CommandException
import net.minecraft.command.CommandSource
import net.minecraft.command.Commands
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.server.MinecraftServer
import net.minecraft.util.math.BlockPos
import net.minecraft.util.text.ITextComponent
import net.minecraft.util.text.Style
import net.minecraft.util.text.TextFormatting
import net.minecraft.util.text.TranslationTextComponent

interface CommandBase {

    fun register(): LiteralArgumentBuilder<CommandSource>

    fun checkPermission(sender: CommandSource): Boolean

    fun commandException(message: String, vararg args: String): Nothing = throw CommandException(
        TranslationTextComponent(message, args)
    )
}