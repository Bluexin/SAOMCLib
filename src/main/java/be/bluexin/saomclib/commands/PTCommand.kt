package be.bluexin.saomclib.commands

import be.bluexin.saomclib.capabilities.getPartyCapability
import net.minecraft.command.CommandBase
import net.minecraft.command.CommandException
import net.minecraft.command.ICommandSender
import net.minecraft.command.WrongUsageException
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.server.MinecraftServer
import net.minecraft.util.text.TextComponentTranslation


/**
 * Part of saomclib by Bluexin.
 *
 * Command used to handle PT-related stuff (invite, accept, decline, kick, leave)
 *
 * @author Bluexin
 */
class PTCommand : CommandBase() {
    override fun getName() = "pt"

    override fun execute(server: MinecraftServer, sender: ICommandSender, args: Array<out String>) {
        if (args.isEmpty()) throw WrongUsageException(getUsage(sender))
        if (sender !is EntityPlayer) throw WrongUsageException("commands.pt.playeronly")
        when (args[0]) {
            "invite" -> handleInvite(server, sender, args)
            "accept" -> handleAccept(server, sender, args)
            "decline" -> handleDecline(server, sender, args)
            "kick" -> handleKick(server, sender, args)
            "leave" -> handleLeave(server, sender, args)
            "cancel" -> handleCancel(server, sender, args)
            "print" -> handlePrint(server, sender, args)
        }
    }

    override fun getUsage(sender: ICommandSender?) = "commands.pt.usage"

    private fun handleInvite(server: MinecraftServer, player: EntityPlayer, args: Array<out String>) {
        if (args.size < 2) throw WrongUsageException("commands.pt.invite.usage")
        val target = getPlayer(server, player, args[1]) // Player not found will interrupt execution
        if (target == player) throw CommandException("commands.pt.invite.self")
        val pt = player.getPartyCapability().getOrCreatePT()
        if (pt.isLeader(player)) {
            if (pt.invite(target)) {
                target.getPartyCapability().invitedTo = pt
                pt.invite(target)
                player.sendMessage(TextComponentTranslation("commands.pt.invite.success", args[1]))
            } else throw CommandException("commands.pt.invite.alreadyPresent", args[1])
        } else throw CommandException("commands.pt.invite.notLeader", pt.leader?.displayNameString ?: "UNKNOWN")
    }

    private fun handleAccept(server: MinecraftServer, player: EntityPlayer, args: Array<out String>) {
        val cap = player.getPartyCapability()
        val invitedTo = cap.invitedTo ?: throw CommandException("commands.pt.accept.notInvited")
        cap.invitedTo = null
        if (invitedTo.isInvited(player)) {
            invitedTo.addMember(player)
            cap.party = invitedTo
            player.sendMessage(TextComponentTranslation("commands.pt.accept.success", invitedTo.leader?.displayNameString ?: "UNKNOWN"))
        } else throw CommandException("commands.pt.accept.notInvited")
    }

    private fun handleDecline(server: MinecraftServer, player: EntityPlayer, args: Array<out String>) {
        val cap = player.getPartyCapability()
        val invitedTo = cap.invitedTo ?: throw CommandException("commands.pt.accept.notInvited")
        cap.invitedTo = null
        if (invitedTo.isInvited(player)) {
            invitedTo.cancel(player)
            player.sendMessage(TextComponentTranslation("commands.pt.decline.success", invitedTo.leader?.displayNameString ?: "UNKNOWN"))
        } else throw CommandException("commands.pt.accept.notInvited")
    }

    private fun handleKick(server: MinecraftServer, player: EntityPlayer, args: Array<out String>) {
        if (args.size < 2) throw WrongUsageException("commands.pt.kick.usage")
        val cap = player.getPartyCapability()
        val pt = cap.getOrCreatePT()
        if (pt.leader == player) {
            val target = getPlayer(server, player, args[1]) // Player not found will interrupt execution
            if (pt.isMember(target)) {
                pt.removeMember(target)
                target.getPartyCapability().party = null
                player.sendMessage(TextComponentTranslation("commands.pt.kick.success", args[1]))
                target.sendMessage(TextComponentTranslation("commands.pt.kick.notification", player.displayNameString))
            }
        } else throw CommandException("commands.pt.kick.notLeader", pt.leader?.displayName ?: "UNKNOWN")
    }

    private fun handleCancel(server: MinecraftServer, player: EntityPlayer, args: Array<out String>) {
        if (args.size < 2) throw WrongUsageException("commands.pt.cancel.usage")
        val cap = player.getPartyCapability()
        val pt = cap.getOrCreatePT()
        if (pt.leader == player) {
            val target = getPlayer(server, player, args[1]) // Player not found will interrupt execution
            if (pt.isInvited(target)) {
                pt.cancel(target)
                target.getPartyCapability().invitedTo = null
                player.sendMessage(TextComponentTranslation("commands.pt.cancel.success", args[1]))
                target.sendMessage(TextComponentTranslation("commands.pt.cancel.notification", player.displayNameString))
            }
        } else throw CommandException("commands.pt.cancel.notLeader", pt.leader?.displayNameString ?: "UNKNOWN")
    }

    private fun handleLeave(server: MinecraftServer, player: EntityPlayer, args: Array<out String>) {
        val cap = player.getPartyCapability()
        val pt = cap.party ?: throw CommandException("commands.pt.leave.notInPT")
        cap.party = null
        if (pt.isMember(player)) {
            pt.removeMember(player)
            player.sendMessage(TextComponentTranslation("commands.pt.leave.success", pt.leader?.displayNameString ?: "UNKNOWN"))
        } else throw CommandException("commands.pt.leave.notInPT")
    }

    private fun handlePrint(server: MinecraftServer, player: EntityPlayer, args: Array<out String>) {
        val pt = player.getPartyCapability().party ?: throw CommandException("commands.pt.leave.notInPT")
        if (!pt.isParty) throw CommandException("commands.pt.leave.notInPT")
        player.sendMessage(TextComponentTranslation("commands.pt.print.output", pt.leader?.displayNameString, pt.members.joinToString { it.displayNameString }))
    }

    override fun getRequiredPermissionLevel() = 0

    override fun checkPermission(server: MinecraftServer?, sender: ICommandSender) = sender is EntityPlayer
}
