package com.tencao.saomclib.commands

/*
enum class CommandList: CommandBase {
    PT {
        override fun register(): LiteralArgumentBuilder<CommandSource> {
            return Commands.literal("pt")
                .requires { it.assertIsEntity() != null }
                .then(Commands.argument("party", TestArgArgument()))
        }

        /*
        override fun getTabCompletions(server: MinecraftServer, sender: ICommandSender, params: Array<String>, pos: BlockPos?): MutableList<String> {
            val commands = PTCommands.values().filter { it.checkPermission(server, sender) }
            val command = commands.firstOrNull{command -> command.getID().equals(params[0], true)}
                    ?: return getListOfStringsMatchingLastWord(params, commands.map { it.getID() })
            return if (params.size > 1) command.getTabCompletions(server, sender, params.drop(1).toTypedArray(), pos)
            else mutableListOf()
        }

        override fun execute(server: MinecraftServer, sender: ICommandSender, params: Array<String>) {
            if (params.isEmpty()) throw WrongUsageException("commands.pt.usage")
            if (sender !is EntityPlayer) throw WrongUsageException("commands.pt.playeronly")

            PTCommands.values().firstOrNull { it.getID().equals(params[0], true) }?.
                    execute(server, sender, params.drop(1).toTypedArray())
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
}*/
*/