package be.bluexin.saomclib.commands

import be.bluexin.saomclib.capabilities.PartyCapability
import be.bluexin.saomclib.capabilities.getPartyCapability
import be.bluexin.saomclib.message
import be.bluexin.saomclib.party.IParty
import net.minecraft.command.CommandBase
import net.minecraft.command.CommandException
import net.minecraft.command.ICommandSender
import net.minecraft.command.WrongUsageException
import net.minecraft.entity.player.EntityPlayer


/**
 * Part of saomclib by Bluexin.
 *
 * Command used to handle PT-related stuff (invite, accept, decline, kick, leave)
 *
 * @author Bluexin
 */
class PTCommand : CommandBase() {

    override fun getCommandName() = "pt"

    override fun processCommand(sender: ICommandSender, args: Array<out String>) {
        if (args.isEmpty()) throw WrongUsageException(getCommandUsage(sender))
        if (sender !is EntityPlayer) throw WrongUsageException("commands.pt.playeronly")
        when (args[0]) {
            "invite" -> handleInvite(sender, args)
            "accept" -> handleAccept(sender, args)
            "decline" -> handleDecline(sender, args)
            "kick" -> handleKick(sender, args)
            "leave" -> handleLeave(sender, args)
            "cancel" -> handleCancel(sender, args)
            "print" -> handlePrint(sender, args)
        }
    }

    override fun getCommandUsage(sender: ICommandSender?) = "commands.pt.usage"

    private fun handleInvite(player: EntityPlayer, args: Array<out String>) {
        if (args.size < 2) throw WrongUsageException("commands.pt.invite.usage")
        val target = getPlayer(player, args[1]) // Player not found will interrupt execution
        if (target == player) throw CommandException("commands.pt.invite.self")
        val pt = player.getPartyCapability().getOrCreatePT()
        if (pt.isLeader(player)) {
            if (pt.invite(target)) {
                target.getPartyCapability().invitedTo = pt
                pt.invite(target)
                player.message("commands.pt.invite.success", args[1])
                target.message("commands.pt.invited", player.displayName)
            } else throw CommandException("commands.pt.invite.alreadyPresent", args[1])
        } else throw CommandException("commands.pt.invite.notLeader", pt.leader?.displayName ?: "UNKNOWN")
    }

    private fun handleAccept(player: EntityPlayer, args: Array<out String>) {
        val cap: PartyCapability = player.getPartyCapability()
        val invitedTo = cap.invitedTo ?: throw CommandException("commands.pt.accept.notInvited")
        cap.invitedTo = null
        if (invitedTo.isInvited(player)) {
            invitedTo.addMember(player)
            cap.party = invitedTo
            player.message("commands.pt.accept.success", invitedTo.leader?.displayName ?: "UNKNOWN")
        } else throw CommandException("commands.pt.accept.notInvited")
    }

    private fun handleDecline(player: EntityPlayer, args: Array<out String>) {
        val cap: PartyCapability = player.getPartyCapability()
        val invitedTo = cap.invitedTo ?: throw CommandException("commands.pt.accept.notInvited")
        cap.invitedTo = null
        if (invitedTo.isInvited(player)) {
            invitedTo.cancel(player)
            player.message("commands.pt.decline.success", invitedTo.leader?.displayName ?: "UNKNOWN")
            invitedTo.leader?.message("commands.pt.declined", player.displayName)
        } else throw CommandException("commands.pt.accept.notInvited")
    }

    private fun handleKick(player: EntityPlayer, args: Array<out String>) {
        if (args.size < 2) throw WrongUsageException("commands.pt.kick.usage")
        val cap: PartyCapability = player.getPartyCapability()
        val pt = cap.getOrCreatePT()
        if (pt.leader == player) {
            val target = getPlayer(player, args[1]) // Player not found will interrupt execution
            if (pt.isMember(target)) {
                pt.removeMember(target)
                target.getPartyCapability().party = null
                player.message("commands.pt.kick.success", args[1])
                target.message("commands.pt.kick.notification", player.displayName)
            }
        } else throw CommandException("commands.pt.kick.notLeader", pt.leader?.displayName ?: "UNKNOWN")
    }

    private fun handleCancel(player: EntityPlayer, args: Array<out String>) {
        if (args.size < 2) throw WrongUsageException("commands.pt.cancel.usage")
        val cap: PartyCapability = player.getPartyCapability()
        val pt = cap.getOrCreatePT()
        if (pt.leader == player) {
            val target = getPlayer(player, args[1]) // Player not found will interrupt execution
            if (pt.isInvited(target)) {
                pt.cancel(target)
                target.getPartyCapability().invitedTo = null
                player.message("commands.pt.cancel.success", args[1])
                target.message("commands.pt.cancel.notification", player.displayName)
            }
        } else throw CommandException("commands.pt.cancel.notLeader", pt.leader?.displayName ?: "UNKNOWN")
    }

    private fun handleLeave(player: EntityPlayer, args: Array<out String>) {
        val cap: PartyCapability = player.getPartyCapability()
        val pt = cap.party ?: throw CommandException("commands.pt.leave.notInPT")
        cap.party = null
        if (pt.isMember(player)) {
            pt.removeMember(player)
            player.message("commands.pt.leave.success", pt.leader?.displayName ?: "UNKNOWN")
        } else throw CommandException("commands.pt.leave.notInPT")
    }

    private fun handlePrint(player: EntityPlayer, args: Array<out String>) {
        val pt: IParty = player.getPartyCapability().party ?: throw CommandException("commands.pt.leave.notInPT")
        if (!pt.isParty) throw CommandException("commands.pt.leave.notInPT")
        player.message("commands.pt.print.output", pt.leader?.displayName ?: "UNKNOWN", pt.members.joinToString { it.displayName })
    }

    override fun getRequiredPermissionLevel() = 0

    override fun canCommandSenderUseCommand(sender: ICommandSender?) = sender is EntityPlayer

    override fun addTabCompletionOptions(sender: ICommandSender, args: Array<out String>): MutableList<*> {
        if (sender !is EntityPlayer) return mutableListOf<String>()
        val cap = sender.getPartyCapability()
        return when (args.size) {
            1 -> {
                val l = mutableListOf("invite")
                if (cap.party != null) {
                    l.add("leave")
                    l.add("print")
                    if (cap.party?.leader == sender) {
                        l.add("kick")
                        l.add("cancel")
                    }
                }
                if (cap.invitedTo != null) {
                    l.add("accept")
                    l.add("decline")
                }
                CommandBase.getListOfStringsMatchingLastWord(args, *l.toTypedArray())
            }
            2 -> {
                val l = mutableListOf<String>()
                when (args[0]) {
                    "leave", "print", "accept", "decline" -> return mutableListOf<String>()
                    "invite" -> {
                        val current = cap.party?.members?.map { it.displayName }
                        val invited = cap.party?.invited?.map { it.displayName }
                        l.addAll(sender.worldObj.playerEntities.map { (it as EntityPlayer).displayName }.filterNot { it == sender.displayName || current?.contains(it) ?: false || invited?.contains(it) ?: false })
                    }
                    "kick" -> {
                        val ll = cap.party?.members?.filterNot { it == sender }?.map { it.displayName }
                        if (ll != null) l.addAll(ll)
                    }
                    "cancel" -> {
                        val ll = cap.party?.invited?.map { it.displayName }
                        if (ll != null) l.addAll(ll)
                    }
                }
                CommandBase.getListOfStringsMatchingLastWord(args, *l.toTypedArray())
            }
            else -> mutableListOf<String>()
        }
    }
}
