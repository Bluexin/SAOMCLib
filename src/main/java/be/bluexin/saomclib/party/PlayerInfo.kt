package be.bluexin.saomclib.party

import be.bluexin.saomclib.SAOMCLib.proxy
import com.google.gson.annotations.SerializedName
import net.minecraft.entity.player.EntityPlayer
import java.lang.ref.WeakReference
import java.util.*

data class PlayerInfo(@SerializedName("UUID") val uuid: UUID) {

    constructor(player: EntityPlayer): this(player.uniqueID){
        this.username = player.displayNameString
    }

    constructor(uuid: UUID, name: String): this(uuid){
        this.username = name
    }

    val uuidString by lazy { uuid.toString() }
    var playerImpl: WeakReference<EntityPlayer>? = null
        get() {
            if (field == null){
                val player = proxy.getPlayerEntity(uuid)
                if (player != null)
                    field = WeakReference(player)
            }
            return field
        }

    val player: EntityPlayer?
        get() = playerImpl?.get()

    val gameProfile = proxy.getGameProfile(uuid)


    //Health cache
    var health: Float = -1f
    get() {
        if (player != null){
            field = -1f
            return player!!.health
        }
        else if (field == -1F) {
            field = proxy.getPlayerHealth(uuid)
        }
        return field
    }

    var maxHealth: Float = -1f
    get() {
        if (player != null){
            field -1f
            return player!!.maxHealth
        }
        else if (field == -1F){
            field = proxy.getPlayerMaxHealth(uuid)
        }
        return field
    }

    @SerializedName("Username") var username: String = ""
        get() {
            if (field.isEmpty())
                field = proxy.getGameProfile(uuid)?.name?: return uuidString
            return field
        }

    override fun equals(other: Any?): Boolean {
        if (other is EntityPlayer)
            return other.uniqueID == uuid
        if (other is PlayerInfo)
            return other.uuid == uuid
        if (other is String)
            return other == uuidString
        if (other is UUID)
            return uuid == other
        return false
    }

    override fun hashCode(): Int {
        return uuid.hashCode()
    }

}

fun EntityPlayer.playerInfo() = PlayerInfo(this)