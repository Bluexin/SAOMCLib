package be.bluexin.saomclib.party

import be.bluexin.saomclib.SAOMCLib
import be.bluexin.saomclib.proxy.CommonProxy
import com.google.gson.annotations.SerializedName
import net.minecraft.entity.player.EntityPlayer
import net.minecraftforge.fml.common.FMLCommonHandler
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
                val player = SAOMCLib.proxy.getPlayerEntity(uuid)
                if (player != null)
                    field = WeakReference(player)
            }
            return field
        }

    val player: EntityPlayer?
        get() = playerImpl?.get()

    @SerializedName("Username") lateinit var username: String


    init {
        if (!::username.isInitialized){
            username = if (SAOMCLib.proxy.getSide() == CommonProxy.ProxySide.SERVER)
                FMLCommonHandler.instance().minecraftServerInstance.playerProfileCache.getProfileByUUID(uuid)?.name?: uuidString
            else
                player?.displayNameString?: uuidString
        }
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