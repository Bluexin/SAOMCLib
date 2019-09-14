package be.bluexin.saomclib.commands

import be.bluexin.saomclib.capabilities.getPartyCapability
import be.bluexin.saomclib.message
import be.bluexin.saomclib.party.IPlayerInfo
import net.minecraft.command.CommandException
import net.minecraft.command.ICommandSender
import net.minecraft.command.WrongUsageException
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.server.MinecraftServer
import net.minecraft.util.math.BlockPos
import net.minecraft.util.text.TextComponentTranslation

enum class PTCommands: CommandBase {
    INVITE {
        override fun getTabCompletions(server: MinecraftServer, sender: ICommandSender, params: Array<String>, pos: BlockPos?): MutableList<String> {
            if (sender !is EntityPlayer) return mutableListOf()
            val cap = sender.getPartyCapability()
            val current = cap.party?.membersInfo?.mapNotNull(IPlayerInfo::player)?.map { it.name }
            val invited = cap.party?.let { party -> party.invitedInfo.mapNotNull { it.key.player } }?.map { it.name }
            val list = server.onlinePlayerNames.filterNot { it == sender.name || current?.contains(it) ?: false || invited?.contains(it) ?: false }
            return getListOfStringsMatchingLastWord(params, list)
        }

        override fun execute(server: MinecraftServer, sender: ICommandSender, params: Array<String>) {
            val player = sender as EntityPlayer
            if (params.isEmpty()) throw WrongUsageException(Command.getUsage(sender))
            val target = net.minecraft.command.CommandBase.getPlayer(server, player, params[0]) // Player not found will interrupt execution
            if (target == player) throw CommandException("commands.pt.invite.self")
            val pt = player.getPartyCapability().getOrCreatePT()
            if (pt.isLeader(player)) {
                if (!pt.isInvited(target)) {
                    pt.invite(target)
                    player.message("commands.pt.invite.success", params[0])
                    target.message("commands.pt.invited", player.displayNameString)
                } else throw CommandException("commands.pt.invite.alreadyPresent", params[0])
            } else throw CommandException("commands.pt.invite.notLeader", pt.leaderInfo?.player?.displayNameString ?: "UNKNOWN")
        }

        override fun checkPermission(server: MinecraftServer, sender: ICommandSender): Boolean {
            return (sender as? EntityPlayer)?.getPartyCapability()?.party?.leaderInfo?.player != sender
        }
    },
    ACCEPT {
        override fun execute(server: MinecraftServer, sender: ICommandSender, params: Array<String>) {
            val player = sender as EntityPlayer
            val cap = player.getPartyCapability()

            val invitedTo = cap.invitedTo ?: throw CommandException("commands.pt.accept.notInvited")
            if (invitedTo.isInvited(player)) {
                invitedTo.addMember(player)
                player.message("commands.pt.accept.success", invitedTo.leaderInfo?.player?.displayNameString ?: "UNKNOWN")
            } else throw CommandException("commands.pt.accept.notInvited")
        }

        override fun checkPermission(server: MinecraftServer, sender: ICommandSender): Boolean {
            return (sender as? EntityPlayer)?.getPartyCapability()?.party?.isInvited(sender)?: false
        }
    },
    DECLINE {
        override fun execute(server: MinecraftServer, sender: ICommandSender, params: Array<String>) {
            val player = sender as EntityPlayer

            val cap = player.getPartyCapability()
            val invitedTo = cap.invitedTo ?: throw CommandException("commands.pt.accept.notInvited")
            if (invitedTo.isInvited(player)) {
                invitedTo.cancel(player)
                player.message("commands.pt.decline.success", invitedTo.leaderInfo?.player?.displayNameString ?: "UNKNOWN")
                invitedTo.leaderInfo?.player?.message("commands.pt.declined", player.displayNameString) // TODO: remove this (should be handled by onReceive)
            } else throw CommandException("commands.pt.accept.notInvited")
        }

        override fun checkPermission(server: MinecraftServer, sender: ICommandSender): Boolean {
            return (sender as? EntityPlayer)?.getPartyCapability()?.party?.isInvited(sender)?: false
        }
    },
    KICK {
        override fun getTabCompletions(server: MinecraftServer, sender: ICommandSender, params: Array<String>, pos: BlockPos?): MutableList<String> {
            if (sender !is EntityPlayer) return mutableListOf()
            val cap = sender.getPartyCapability()

            val list = cap.party?.membersInfo?.mapNotNull(IPlayerInfo::player)?.filterNot { it == sender }?.map { it.name }?.toList()?: return mutableListOf()
            return getListOfStringsMatchingLastWord(params, list)
        }

        override fun execute(server: MinecraftServer, sender: ICommandSender, params: Array<String>) {
            val player = sender as EntityPlayer

            if (params.isEmpty()) throw WrongUsageException(Command.getUsage(sender))
            val cap = player.getPartyCapability()
            val pt = cap.getOrCreatePT()
            if (pt.leaderInfo?.player == player) {
                val target = net.minecraft.command.CommandBase.getPlayer(server, player, params[0]) // Player not found will interrupt execution
                if (pt.isMember(target)) {
                    pt.removeMember(target)
                    player.message("commands.pt.kick.success", params[0])
                    target.message("commands.pt.kick.notification", player.displayNameString)
                }
            } else throw CommandException("commands.pt.kick.notLeader", pt.leaderInfo?.player?.displayName ?: "UNKNOWN")
        }

        override fun checkPermission(server: MinecraftServer, sender: ICommandSender): Boolean {
            return (sender as? EntityPlayer)?.getPartyCapability()?.party?.leaderInfo?.player == sender
        }
    },
    LEAVE {
        override fun execute(server: MinecraftServer, sender: ICommandSender, params: Array<String>) {
            val player = sender as EntityPlayer

            val cap = player.getPartyCapability()
            val pt = cap.party ?: throw CommandException("commands.pt.leave.notInPT")
            if (pt.isMember(player)) {
                pt.removeMember(player)
                player.message("commands.pt.leave.success", pt.leaderInfo?.player?.displayNameString ?: "UNKNOWN")
            } else throw CommandException("commands.pt.leave.notInPT")
        }

        override fun checkPermission(server: MinecraftServer, sender: ICommandSender): Boolean {
            return (sender as? EntityPlayer)?.getPartyCapability()?.party != null
        }
    },
    CANCEL {
        override fun getTabCompletions(server: MinecraftServer, sender: ICommandSender, params: Array<String>, pos: BlockPos?): MutableList<String> {
            if (sender !is EntityPlayer) return mutableListOf()
            val cap = sender.getPartyCapability()

            val list = cap.party?.let { party -> party.invitedInfo.mapNotNull { it.key.player } }?.map { it.name }?.toList()?: return mutableListOf()
            return getListOfStringsMatchingLastWord(params, list)
        }

        override fun execute(server: MinecraftServer, sender: ICommandSender, params: Array<String>) {
            val player = sender as EntityPlayer

            if (params.isEmpty()) throw WrongUsageException(Command.getUsage(sender))
            val cap = player.getPartyCapability()
            val pt = cap.getOrCreatePT()
            if (pt.leaderInfo?.player == player) {
                val target = net.minecraft.command.CommandBase.getPlayer(server, player, params[0]) // Player not found will interrupt execution
                if (pt.isInvited(target)) {
                    pt.cancel(target)
                    player.message("commands.pt.cancel.success", params[0])
                    target.message("commands.pt.cancel.notification", player.displayNameString)
                }
            } else throw CommandException("commands.pt.cancel.notLeader", pt.leaderInfo?.player?.displayNameString ?: "UNKNOWN")
        }

        override fun checkPermission(server: MinecraftServer, sender: ICommandSender): Boolean {
            val party = (sender as? EntityPlayer)?.getPartyCapability()?.party?: return false
            return party.leaderInfo?.player == sender && party.invitedInfo.count() > 0
        }
    },
    PRINT {
        override fun execute(server: MinecraftServer, sender: ICommandSender, params: Array<String>) {
            val player = sender as EntityPlayer

            val cap = player.getPartyCapability()
            val pt = cap.party
            val invited = cap.invitedTo
            var ok = false
            if (pt?.isParty == true) {
                ok = true
                player.message("commands.pt.print.output", pt.leaderInfo?.player?.displayNameString
                        ?: "UNKNOWN", pt.membersInfo.mapNotNull(IPlayerInfo::player).joinToString { it.displayNameString })
            }
            if (invited?.isParty == true) {
                ok = true
                player.message("commands.pt.print.output", invited.leaderInfo?.player?.displayNameString
                        ?: "UNKNOWN", invited.membersInfo.mapNotNull(IPlayerInfo::player).joinToString { it.displayNameString })
            }
            if (!ok) throw CommandException("commands.pt.leave.notInPT")
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
    }
}