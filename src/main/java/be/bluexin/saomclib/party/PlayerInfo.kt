package be.bluexin.saomclib.party

import be.bluexin.saomclib.SAOMCLib.proxy
import com.google.gson.*
import com.google.gson.annotations.JsonAdapter
import com.google.gson.annotations.SerializedName
import com.mojang.authlib.GameProfile
import net.minecraft.entity.player.EntityPlayer
import java.lang.ref.WeakReference
import java.lang.reflect.Type
import java.util.*

@JsonAdapter(PlayerInfoSerializer::class)
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


    //Health cache
    val health: Float
    get() {
        return player?.health?: proxy.getPlayerHealth(uuid)?: 0f
    }

    val maxHealth: Float
    get() {
        return player?.maxHealth?: proxy.getPlayerMaxHealth(uuid)?: 20f
    }

    @SerializedName("Username") var username: String = ""
        get() {
            if (field.isEmpty())
                field = proxy.getGameProfile(uuid)?.name?: return uuidString
            return field
        }

    val gameProfile: GameProfile = proxy.getGameProfile(uuid)?: GameProfile(uuid, username)

    val isOnline = proxy.isPlayerOnline(uuid)

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

    companion object{
        val EMPTY = PlayerInfo(UUID.fromString("00000000-0000-0000-0000-000000000000"))

        val gson = GsonBuilder().create()
    }

}

object PlayerInfoSerializer: JsonSerializer<PlayerInfo>, JsonDeserializer<PlayerInfo>{
    override fun serialize(src: PlayerInfo, typeOfSrc: Type, context: JsonSerializationContext): JsonElement {
        return JsonPrimitive("${src.uuidString}:${src.username}")
    }

    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): PlayerInfo {
        val data = json.asJsonPrimitive.asString.split(":")
        return PlayerInfo(UUID.fromString(data.first()), data.last())
    }
}

fun EntityPlayer.playerInfo() = PlayerInfo(this)