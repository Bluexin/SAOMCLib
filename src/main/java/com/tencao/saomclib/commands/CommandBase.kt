package com.tencao.saomclib.commands

import com.mojang.brigadier.builder.LiteralArgumentBuilder
import net.minecraft.command.CommandException
import net.minecraft.command.CommandSource
import net.minecraft.util.text.TranslationTextComponent

interface CommandBase {

    fun register(): LiteralArgumentBuilder<CommandSource>

    fun checkPermission(sender: CommandSource): Boolean

    fun commandException(message: String, vararg args: String): Nothing = throw CommandException(
        TranslationTextComponent(message, args)
    )
}
