package com.tencao.saomclib.commands

import com.tencao.saomclib.message
import com.tencao.saomclib.party.IParty
import com.tencao.saomclib.party.PartyManager
import com.tencao.saomclib.party.PlayerInfo
import com.tencao.saomclib.party.playerInfo
import net.minecraft.command.CommandException
import net.minecraft.command.ICommandSender
import net.minecraft.command.WrongUsageException
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.server.MinecraftServer
import net.minecraft.util.math.BlockPos
import net.minecraft.util.text.TextComponentTranslation

enum class PTCommands : CommandBase {
    INVITE {
        override fun getTabCompletions(server: MinecraftServer, sender: ICommandSender, params: Array<String>, pos: BlockPos?): MutableList<String> {
            if (sender !is EntityPlayer) return mutableListOf()
            val party = PartyManager.getPartyObject(sender.playerInfo())
            val current = party?.membersInfo?.mapNotNull(PlayerInfo::player)?.map { it.name } ?: emptyList()
            val invited = party?.invitedInfo?.mapNotNull { it.key.player }?.map { it.name } ?: emptyList()
            val list = server.onlinePlayerNames.filterNot { it == sender.name || current.contains(it) || invited.contains(it) }
            return getListOfStringsMatchingLastWord(params, list)
        }

        override fun execute(server: MinecraftServer, sender: ICommandSender, params: Array<String>) {
            val player = sender as EntityPlayer
            if (params.isEmpty()) throw WrongUsageException(Command.getUsage(sender))
            val target = net.minecraft.command.CommandBase.getPlayer(server, player, params[0]) // Player not found will interrupt execution
            if (target == player) throw CommandException("commands.pt.invite.self")
            val pt = PartyManager.getOrCreateParty(PlayerInfo(sender))
            if (pt.isLeader(player)) {
                if (!pt.isInvited(target)) {
                    pt.invite(target)
                } else throw CommandException("commands.pt.invite.alreadyPresent", params[0])
            } else throw CommandException("commands.pt.invite.notLeader", pt.leaderInfo.username)
        }

        override fun checkPermission(server: MinecraftServer, sender: ICommandSender): Boolean {
            return !checkInParty(sender) || checkIfLeader(sender)
        }
    },
    ACCEPT {
        override fun execute(server: MinecraftServer, sender: ICommandSender, params: Array<String>) {
            val player = sender as EntityPlayer
            val party = if (params.isEmpty()) PartyManager.getInvitedParties(sender.playerInfo()).first() else
                PartyManager.getInvitedParties(sender.playerInfo()).firstOrNull { it.leaderInfo.username == params[0] }?:
                throw CommandException("commands.pt.invite.notFound")

            if (party.isInvited(player)) {
                party.addMember(player)
                player.message("commands.pt.accept.success", party.leaderInfo.username)
            } else throw CommandException("commands.pt.accept.notInvited")
        }

        override fun checkPermission(server: MinecraftServer, sender: ICommandSender): Boolean {
            return checkIfInvited(sender)
        }

        override fun getTabCompletions(server: MinecraftServer, sender: ICommandSender, params: Array<String>, pos: BlockPos?): MutableList<String> {
            if (sender !is EntityPlayer) return mutableListOf()
            val parties = PartyManager.getInvitedParties(sender.playerInfo())

            val list = parties.map { it.leaderInfo.username }.toList()
            return getListOfStringsMatchingLastWord(params, list)
        }
    },
    DECLINE {
        override fun execute(server: MinecraftServer, sender: ICommandSender, params: Array<String>) {
            val player = sender as EntityPlayer

            val party = if (params.isEmpty()) PartyManager.getInvitedParties(sender.playerInfo()).first() else
                PartyManager.getInvitedParties(sender.playerInfo()).firstOrNull { it.leaderInfo.username == params[0] }?:
                throw CommandException("commands.pt.invite.notFound")

            if (party.isInvited(player)) {
                party.cancel(player)
            } else throw CommandException("commands.pt.accept.notInvited")
        }

        override fun checkPermission(server: MinecraftServer, sender: ICommandSender): Boolean {
            return checkIfInvited(sender)
        }

        override fun getTabCompletions(server: MinecraftServer, sender: ICommandSender, params: Array<String>, pos: BlockPos?): MutableList<String> {
            if (sender !is EntityPlayer) return mutableListOf()
            val parties = PartyManager.getInvitedParties(sender.playerInfo())

            val list = parties.map { it.leaderInfo.username }.toList()
            return getListOfStringsMatchingLastWord(params, list)
        }
    },
    KICK {
        override fun getTabCompletions(server: MinecraftServer, sender: ICommandSender, params: Array<String>, pos: BlockPos?): MutableList<String> {
            if (sender !is EntityPlayer) return mutableListOf()
            val party = PartyManager.getPartyObject(sender.playerInfo()) ?: return mutableListOf()

            val list = party.membersInfo.mapNotNull(PlayerInfo::player).filterNot { it == sender }.map { it.name }.toList()
            return getListOfStringsMatchingLastWord(params, list)
        }

        override fun execute(server: MinecraftServer, sender: ICommandSender, params: Array<String>) {
            val player = sender as EntityPlayer

            if (params.isEmpty()) throw WrongUsageException(Command.getUsage(sender))
            val party = PartyManager.getPartyObject(sender.playerInfo()) ?: throw CommandException("commands.pt.leave.notInPT")
            if (party.leaderInfo.equals(player)) {
                val target = net.minecraft.command.CommandBase.getPlayer(server, player, params[0]) // Player not found will interrupt execution
                if (party.isMember(target)) {
                    party.removeMember(target)
                    player.message("commands.pt.kick.success", params[0])
                    target.message("commands.pt.kick.notification", player.displayNameString)
                }
            } else throw CommandException("commands.pt.kick.notLeader", party.leaderInfo.username)
        }

        override fun checkPermission(server: MinecraftServer, sender: ICommandSender): Boolean {
            return checkIfLeader(sender)
        }
    },
    LEAVE {
        override fun execute(server: MinecraftServer, sender: ICommandSender, params: Array<String>) {
            val player = sender as EntityPlayer

            val party = PartyManager.getPartyObject(player.playerInfo()) ?: throw CommandException("commands.pt.leave.notInPT")
            if (party.isMember(player)) {
                party.removeMember(player)
                if (party.isParty || !party.isLeader(player)) {
                    player.message("commands.pt.leave.success", party.leaderInfo.username)
                } else {
                    player.message("commands.pt.leave.disband")
                }
            } else throw CommandException("commands.pt.leave.notInPT")
        }

        override fun checkPermission(server: MinecraftServer, sender: ICommandSender): Boolean {
            return checkInParty(sender)
        }
    },
    CANCEL {
        override fun getTabCompletions(server: MinecraftServer, sender: ICommandSender, params: Array<String>, pos: BlockPos?): MutableList<String> {
            if (sender !is EntityPlayer) return mutableListOf()
            val party = PartyManager.getPartyObject(sender.playerInfo()) ?: return mutableListOf()

            val list = party.invitedInfo.mapNotNull { it.key.player }.map { it.name }.toList()
            return getListOfStringsMatchingLastWord(params, list)
        }

        override fun execute(server: MinecraftServer, sender: ICommandSender, params: Array<String>) {
            val player = sender as EntityPlayer

            if (params.isEmpty()) throw WrongUsageException(Command.getUsage(sender))
            val party = PartyManager.getPartyObject(player.playerInfo()) ?: throw CommandException("commands.pt.leave.notInPT")
            if (party.leaderInfo.player == player) {
                val target = net.minecraft.command.CommandBase.getPlayer(server, player, params[0]) // Player not found will interrupt execution
                if (party.isInvited(target)) {
                    party.cancel(target)
                    player.message("commands.pt.cancel.success", params[0])
                    target.message("commands.pt.cancel.notification", player.displayNameString)
                }
            } else throw CommandException("commands.pt.cancel.notLeader", party.leaderInfo.username)
        }

        override fun checkPermission(server: MinecraftServer, sender: ICommandSender): Boolean {
            return checkAnyInvited(sender)
        }
    },
    PRINT {
        override fun execute(server: MinecraftServer, sender: ICommandSender, params: Array<String>) {
            val player = sender as EntityPlayer
            val parties: MutableList<IParty> = mutableListOf()
            PartyManager.getPartyObject(sender.playerInfo())?.let {
                parties.add(it)
            }
            parties.addAll(PartyManager.getInvitedParties(sender.playerInfo()))
            if (parties.isNotEmpty()) {
                parties.forEach {party ->
                    player.message("commands.pt.print.output", party.leaderInfo.username, party.membersInfo.mapNotNull(PlayerInfo::player).joinToString { it.displayNameString })
                }
            }
            else {
                throw CommandException("commands.pt.leave.notInPT")
            }
        }
    };

    override fun getID(): String {
        return TextComponentTranslation("commands.pt.${name.toLowerCase()}").unformattedText
    }

    override fun getUsage(sender: ICommandSender): String {
        return "commands.pt.${name.toLowerCase()}.usage"
    }

    override fun checkPermission(server: MinecraftServer, sender: ICommandSender) = sender is EntityPlayer

    companion object {
        val commands = values().map { it.getID() }

        /**
         * Checks if this player is in a valid party
         */
        fun checkInParty(sender: ICommandSender): Boolean {
            val player = (sender as? EntityPlayer) ?: return false
            return PartyManager.getPartyObject(player.playerInfo())?.isParty == true
        }

        /**
         * Checks if this player has permission to do
         * general party management commands.
         */
        fun checkIfLeader(sender: ICommandSender): Boolean {
            return if (checkInParty(sender)) {
                PartyManager.getPartyObject((sender as EntityPlayer).playerInfo())?.leaderInfo?.uuid == sender.uniqueID
            } else false
        }

        /**
         * Checks if the sender has been invited to any parties
         */
        fun checkIfInvited(sender: ICommandSender): Boolean {
            val player = sender as? EntityPlayer ?: return false
            return PartyManager.getInvitedParties(player.playerInfo()).isNotEmpty()
        }

        /**
         * Checks if the party has any pending invites sent
         */
        fun checkAnyInvited(sender: ICommandSender): Boolean {
            return checkIfLeader(sender) && PartyManager.getPartyObject((sender as EntityPlayer).playerInfo())!!.invitedInfo.isNotEmpty()
        }
    }
}
