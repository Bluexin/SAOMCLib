package com.tencao.saomclib.party

import com.google.gson.*
import com.google.gson.annotations.JsonAdapter
import com.google.gson.annotations.SerializedName
import com.mojang.authlib.GameProfile
import com.tencao.saomclib.SAOMCLib.proxy
import net.minecraft.entity.player.EntityPlayer
import java.lang.ref.WeakReference
import java.lang.reflect.Type
import java.util.*

@JsonAdapter(PlayerInfoSerializer::class)
data class PlayerInfo(@SerializedName("UUID") val uuid: UUID) {

    constructor(player: EntityPlayer) : this(player.uniqueID) {
        this.username = player.displayNameString
    }

    constructor(uuid: UUID, name: String) : this(uuid) {
        this.username = name
    }

    constructor(profile: GameProfile) : this(profile.id) {
        username = profile.name
        gameProfile = profile
    }

    val uuidString by lazy { uuid.toString() }
    var playerImpl: WeakReference<EntityPlayer>? = null
        get() {
            if (field == null) {
                val player = proxy.getPlayerEntity(uuid)
                if (player != null) {
                    field = WeakReference(player)
                }
            }
            return field
        }

    val player: EntityPlayer?
        get() = playerImpl?.get()

    // Health cache
    val health: Float
        get() {
            return player?.health ?: proxy.getPlayerHealth(uuid)
        }

    val maxHealth: Float
        get() {
            return player?.maxHealth ?: proxy.getPlayerMaxHealth(uuid)
        }

    @SerializedName("Username")
    var username: String = ""
        get() {
            if (field.isEmpty()) {
                field = proxy.getGameProfile(uuid)?.name ?: return uuidString
            }
            return field
        }

    var gameProfile: GameProfile = proxy.getGameProfile(uuid) ?: GameProfile(uuid, username)

    val isOnline: Boolean
        get() = proxy.isPlayerOnline(uuid)

    fun equals(other: EntityPlayer): Boolean {
        return other.uniqueID == uuid
    }

    fun equals(other: PlayerInfo): Boolean {
        return other.uuid == uuid
    }

    fun equals(other: String): Boolean {
        return other == uuidString
    }

    fun equals(other: UUID): Boolean {
        return uuid == other
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PlayerInfo

        if (uuid != other.uuid) return false

        return true
    }

    override fun hashCode(): Int {
        return uuid.hashCode()
    }

    companion object {
        val EMPTY = PlayerInfo(UUID( 0, 0))

        val gson: Gson = GsonBuilder().create()
    }
}

object PlayerInfoSerializer : JsonSerializer<PlayerInfo>, JsonDeserializer<PlayerInfo> {
    override fun serialize(src: PlayerInfo, typeOfSrc: Type, context: JsonSerializationContext): JsonElement {
        return JsonPrimitive("${src.uuidString}:${src.username}")
    }

    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): PlayerInfo {
        val data = json.asJsonPrimitive.asString.split(":")
        return PlayerInfo(UUID.fromString(data.first()), data.last())
    }
}

fun EntityPlayer.playerInfo() = PlayerInfo(this)

fun UUID.toPlayerInfo() = PlayerInfo(this)
