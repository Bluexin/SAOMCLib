package com.tencao.saomclib.commands

import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import com.tencao.saomclib.message
import com.tencao.saomclib.party.PartyManager
import com.tencao.saomclib.party.PlayerInfo
import com.tencao.saomclib.party.playerInfo
import net.minecraft.command.CommandSource
import net.minecraft.command.Commands
import net.minecraft.command.arguments.EntityArgument
import net.minecraft.entity.player.PlayerEntity

enum class PTCommands : CommandBase {
    INVITE {
        override fun register(): LiteralArgumentBuilder<CommandSource> {
            return Commands.literal("invite")
                .requires { cs -> checkPermission(cs) }
                .then(
                    Commands.argument("player", EntityArgument.player())
                        .executes { ctx ->
                            execute(ctx, EntityArgument.getPlayer(ctx, "player"))
                        }
                )
        }

        /*
        override fun getTabCompletions(server: MinecraftServer, sender: ICommandSender, params: Array<String>, pos: BlockPos?): MutableList<String> {
            if (sender !is EntityPlayer) return mutableListOf()
            val party = PartyManager.getPartyObject(sender.playerInfo())
            val current = party?.membersInfo?.mapNotNull(PlayerInfo::player)?.map { it.name }?: emptyList()
            val invited = party?.invitedInfo?.mapNotNull { it.key.player }?.map { it.name }?: emptyList()
            val list = server.onlinePlayerNames.filterNot { it == sender.name || current.contains(it) || invited.contains(it)}
            return getListOfStringsMatchingLastWord(params, list)
        }*/

        fun execute(ctx: CommandContext<CommandSource>, target: PlayerEntity): Int {
            val player = ctx.source.asPlayer()
            if (target == player) commandException("commands.pt.invite.self")
            val pt = PartyManager.getOrCreateParty(player.playerInfo())
            if (pt.isLeader(player)) {
                if (!pt.isInvited(target)) {
                    pt.invite(target)
                } else {
                    commandException("commands.pt.invite.alreadyPresent", target.scoreboardName)
                }
            } else commandException("commands.pt.invite.notLeader", pt.leaderInfo.username)
            return com.mojang.brigadier.Command.SINGLE_SUCCESS
        }

        override fun checkPermission(sender: CommandSource): Boolean {
            return (!checkInParty(sender) || checkIfLeader(sender))
        }
    },
    ACCEPT {

        override fun register(): LiteralArgumentBuilder<CommandSource> {
            return Commands.literal("accept")
                .requires { cs -> checkPermission(cs) }
                .executes { ctx ->
                    execute(ctx)
                }
        }

        fun execute(ctx: CommandContext<CommandSource>): Int {
            val player = ctx.source.asPlayer()
            val party = PartyManager.getInvitedParty(player.playerInfo()) ?: commandException("commands.pt.accept.notInvited")

            if (party.isInvited(player)) {
                party.addMember(player)
                player.message("commands.pt.accept.success", party.leaderInfo.username)
            } else commandException("commands.pt.accept.notInvited")
            return com.mojang.brigadier.Command.SINGLE_SUCCESS
        }

        override fun checkPermission(sender: CommandSource): Boolean {
            return checkIfInvited(sender)
        }
    },
    DECLINE {
        override fun register(): LiteralArgumentBuilder<CommandSource> {
            return Commands.literal("pt")
                .then(Commands.literal("decline"))
                .requires { cs -> checkPermission(cs) }
                .executes { ctx ->
                    execute(ctx)
                }
        }

        fun execute(ctx: CommandContext<CommandSource>): Int {
            val player = ctx.source.asPlayer()

            val party = PartyManager.getInvitedParty(player.playerInfo()) ?: commandException("commands.pt.accept.notInvited")
            if (party.isInvited(player)) {
                party.cancel(player)
            } else commandException("commands.pt.accept.notInvited")
            return com.mojang.brigadier.Command.SINGLE_SUCCESS
        }

        override fun checkPermission(sender: CommandSource): Boolean {
            return checkIfInvited(sender)
        }
    },
    KICK {
        override fun register(): LiteralArgumentBuilder<CommandSource> {
            return Commands.literal("kick")
                .requires { cs -> checkPermission(cs) }
                .then(
                    Commands.argument("player", EntityArgument.player())
                        .executes { ctx ->
                            execute(ctx, EntityArgument.getPlayer(ctx, "player"))
                        }
                )
        }

        /*
        override fun getTabCompletions(server: MinecraftServer, sender: ICommandSender, params: Array<String>, pos: BlockPos?): MutableList<String> {
            if (sender !is EntityPlayer) return mutableListOf()
            val party = PartyManager.getPartyObject(sender.playerInfo())?: return mutableListOf()

            val list = party.membersInfo.mapNotNull(PlayerInfo::player).filterNot { it == sender }.map { it.name }.toList()
            return getListOfStringsMatchingLastWord(params, list)
        }*/

        fun execute(ctx: CommandContext<CommandSource>, target: PlayerEntity): Int {
            val player = ctx.source.asPlayer()
            val party = PartyManager.getPartyObject(player.playerInfo()) ?: commandException("commands.pt.leave.notInPT")
            if (party.leaderInfo.equals(player)) {
                if (party.isMember(target)) {
                    party.removeMember(target)
                    player.message("commands.pt.kick.success", target.scoreboardName)
                    target.message("commands.pt.kick.notification", player.scoreboardName)
                }
            } else commandException("commands.pt.kick.notLeader", party.leaderInfo.username)
            return com.mojang.brigadier.Command.SINGLE_SUCCESS
        }

        override fun checkPermission(sender: CommandSource): Boolean {
            return checkIfLeader(sender)
        }
    },
    LEAVE {
        override fun register(): LiteralArgumentBuilder<CommandSource> {
            return Commands.literal("leave")
                .requires { cs -> checkPermission(cs) }
                .executes { ctx ->
                    execute(ctx)
                }
        }

        fun execute(ctx: CommandContext<CommandSource>): Int {
            val player = ctx.source.asPlayer()

            val party = PartyManager.getPartyObject(player.playerInfo()) ?: commandException("commands.pt.leave.notInPT")
            if (party.isMember(player)) {
                party.removeMember(player)
                if (party.isParty || !party.isLeader(player)) {
                    player.message("commands.pt.leave.success", party.leaderInfo.username)
                } else {
                    player.message("commands.pt.leave.disband")
                }
            } else commandException("commands.pt.leave.notInPT")
            return com.mojang.brigadier.Command.SINGLE_SUCCESS
        }

        override fun checkPermission(sender: CommandSource): Boolean {
            return checkInParty(sender)
        }
    },
    CANCEL {
        override fun register(): LiteralArgumentBuilder<CommandSource> {
            return Commands.literal("cancel")
                .requires { cs -> checkPermission(cs) }
                .then(
                    Commands.argument("player", EntityArgument.player())
                        .executes { ctx ->
                            execute(ctx, EntityArgument.getPlayer(ctx, "player"))
                        }
                )
        }

        /*
        override fun getTabCompletions(server: MinecraftServer, sender: ICommandSender, params: Array<String>, pos: BlockPos?): MutableList<String> {
            if (sender !is EntityPlayer) return mutableListOf()
            val party = PartyManager.getPartyObject(sender.playerInfo())?: return mutableListOf()

            val list = party.invitedInfo.mapNotNull { it.key.player }.map { it.name }.toList()
            return getListOfStringsMatchingLastWord(params, list)
        }*/

        fun execute(ctx: CommandContext<CommandSource>, target: PlayerEntity): Int {
            val player = ctx.source.asPlayer()

            val party = PartyManager.getPartyObject(player.playerInfo()) ?: commandException("commands.pt.leave.notInPT")
            if (party.leaderInfo.player == player) {
                if (party.isInvited(target)) {
                    party.cancel(target)
                    player.message("commands.pt.cancel.success", target.scoreboardName)
                    target.message("commands.pt.cancel.notification", player.scoreboardName)
                }
            } else commandException("commands.pt.cancel.notLeader", party.leaderInfo.username)
            return com.mojang.brigadier.Command.SINGLE_SUCCESS
        }

        override fun checkPermission(sender: CommandSource): Boolean {
            return checkAnyInvited(sender)
        }
    },
    PRINT {

        override fun register(): LiteralArgumentBuilder<CommandSource> {
            return Commands.literal("print")
                .requires { cs -> checkPermission(cs) }
                .executes { ctx ->
                    execute(ctx)
                }
        }
        fun execute(ctx: CommandContext<CommandSource>): Int {
            val player = ctx.source.asPlayer()

            val party = PartyManager.getPartyObject(player.playerInfo()) ?: PartyManager.getInvitedParty(player.playerInfo()) ?: commandException("commands.pt.leave.notInPT")
            if (party.isParty) {
                player.message(
                    "commands.pt.print.output",
                    party.leaderInfo.username,
                    party.membersInfo.mapNotNull(
                        PlayerInfo::player
                    ).joinToString { it.scoreboardName }
                )
            }
            return com.mojang.brigadier.Command.SINGLE_SUCCESS
        }

        override fun checkPermission(sender: CommandSource): Boolean {
            return checkInParty(sender)
        }
    };

    companion object {

        /**
         * Checks if this player is in a valid party
         */
        fun checkInParty(sender: CommandSource): Boolean {
            val player = (sender.asPlayer()) ?: return false
            return PartyManager.getPartyObject(player.playerInfo())?.isParty == true
        }

        /**
         * Checks if this player has permission to do
         * general party management commands.
         */
        fun checkIfLeader(sender: CommandSource): Boolean {
            return if (checkInParty(sender)) {
                PartyManager.getPartyObject(sender.asPlayer().playerInfo())?.leaderInfo?.uuid == sender.asPlayer().uniqueID
            } else false
        }

        /**
         * Checks if the sender has been invited to any parties
         */
        fun checkIfInvited(sender: CommandSource): Boolean {
            val player = sender.asPlayer() ?: return false
            return PartyManager.getInvitedParty(player.playerInfo()) != null
        }

        /**
         * Checks if the party has any pending invites sent
         */
        fun checkAnyInvited(sender: CommandSource): Boolean {
            return checkIfLeader(sender) && PartyManager.getPartyObject((sender.asPlayer()).playerInfo())!!.invitedInfo.count() > 0
        }
    }
}
