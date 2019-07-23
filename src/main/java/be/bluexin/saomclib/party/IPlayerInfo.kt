package be.bluexin.saomclib.party

import net.minecraft.entity.player.EntityPlayer
import net.minecraft.world.World
import java.lang.ref.WeakReference
import java.util.*

interface IPlayerInfo {

    val uuid: UUID
    val uuidString: String

    val username: String

    val present: Boolean
        get() = player != null

    val player: EntityPlayer?

    fun tryLoad(player: EntityPlayer): Boolean
}

data class PlayerInfo(
        override val uuid: UUID
) : IPlayerInfo {
    constructor(player: EntityPlayer) : this(player.uniqueID) {
        this.playerImpl = WeakReference(player)
        this.username = player.displayNameString
    }

    constructor(uuid: UUID, world: World) : this(uuid) {
        val player = world.getPlayerEntityByUUID(uuid) ?: return
        this.playerImpl = WeakReference(player)
        this.username = player.displayNameString
    }

    override val uuidString = uuid.toString()
    override var username: String = uuid.toString()
    private var playerImpl: WeakReference<EntityPlayer>? = null

    override val player: EntityPlayer?
        get() = playerImpl?.get()

    override fun tryLoad(player: EntityPlayer): Boolean {
        if (player.uniqueID == this.uuid) {
            this.playerImpl = WeakReference(player)
            this.username = player.displayNameString
            return true
        }
        return false
    }

    override fun equals(other: Any?) = this === other || (other is IPlayerInfo && uuid == other.uuid)
    override fun hashCode() = uuid.hashCode()
}