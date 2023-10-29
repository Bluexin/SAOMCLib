@file:JvmMultifileClass
@file:JvmName("CommonUtilMethods")
@file:Suppress("unused")

package com.tencao.saomclib.utils

import net.minecraft.item.ItemStack
import net.minecraft.nbt.*
import net.minecraft.util.IStringSerializable
import net.minecraft.util.ResourceLocation
import net.minecraftforge.common.util.Constants
import net.minecraftforge.common.util.INBTSerializable
import java.util.*
import java.util.function.Consumer

// ===================================================================================================== Generic Helpers

private inline fun <T : Any, K, E> T?.getIf(key: K, predicate: T?.(K) -> Boolean, get: T.(K) -> E): E? =
    getIf(key, predicate, get, null)

private inline fun <T : Any, K, E> T?.getIf(key: K, predicate: T?.(K) -> Boolean, get: T.(K) -> E, default: E): E {
    if (this != null && predicate(key)) {
        return get(key)
    }
    return default
}

// ========================================================================================================= NBT Helpers

fun Class<out INBT>.idForClass() = when (this) {
    ByteNBT::class.java -> 1
    ShortNBT::class.java -> 2
    IntNBT::class.java -> 3
    LongNBT::class.java -> 4
    FloatNBT::class.java -> 5
    DoubleNBT::class.java -> 6
    ByteArrayNBT::class.java -> 7
    StringNBT::class.java -> 8
    ListNBT::class.java -> 9
    CompoundNBT::class.java -> 10
    IntArrayNBT::class.java -> 11
    LongArrayNBT::class.java -> 12
    else -> throw IllegalArgumentException("Unknown NBT type: $this")
}

fun Int.nbtClassForId(): Class<out INBT> {
    return when (this) {
        1 -> ByteNBT::class.java
        2 -> ShortNBT::class.java
        3 -> IntNBT::class.java
        4 -> LongNBT::class.java
        5 -> FloatNBT::class.java
        6 -> DoubleNBT::class.java
        7 -> ByteArrayNBT::class.java
        8 -> StringNBT::class.java
        9 -> ListNBT::class.java
        10 -> CompoundNBT::class.java
        11 -> IntArrayNBT::class.java
        12 -> LongArrayNBT::class.java
        else -> throw IllegalArgumentException("Unknown NBT type: $this")
    }
}

inline fun <reified T : INBT> INBT.castOrDefault(): T = this.castOrDefault(T::class.java)

@Suppress("UNCHECKED_CAST")
fun <T : INBT> INBT.castOrDefault(clazz: Class<T>): T {
    return (
        when {
            clazz.isAssignableFrom(this.javaClass) -> this
            else -> clazz.defaultNBTValue()
        }
        ) as T
}

inline fun <reified T : INBT> defaultNBTValue(): T = T::class.java.defaultNBTValue()

@Suppress("UNCHECKED_CAST")
fun <T : INBT> Class<T>.defaultNBTValue(): T {
    return (
        when {
            NumberNBT::class.java.isAssignableFrom(this) -> when (this) {
                LongNBT::class.java -> LongNBT.valueOf(0)
                IntNBT::class.java -> IntNBT.valueOf(0)
                ShortNBT::class.java -> ShortNBT.valueOf(0)
                DoubleNBT::class.java -> DoubleNBT.valueOf(0.0)
                FloatNBT::class.java -> FloatNBT.valueOf(0f)
                else -> ByteNBT.valueOf(0)
            }
            this == ByteArrayNBT::class.java -> ByteArrayNBT(ByteArray(0))
            this == StringNBT::class.java -> StringNBT.valueOf("")
            this == ListNBT::class.java -> ListNBT()
            this == CompoundNBT::class.java -> CompoundNBT()
            this == IntArrayNBT::class.java -> IntArrayNBT(IntArray(0))
            this == LongArrayNBT::class.java -> LongArrayNBT(LongArray(0))
            else -> throw IllegalArgumentException("Unknown NBT type to produce: $this")
        }
        ) as T
}

// ===================================================================================================== NBTTagCompound?

fun CompoundNBT?.removeTag(tag: String) = this?.remove(tag)

fun CompoundNBT?.hasNumericKey(tag: String) = this.hasKey(tag, Constants.NBT.TAG_ANY_NUMERIC)
fun CompoundNBT?.hasKey(tag: String) = this != null && this.contains(tag)
fun CompoundNBT?.hasKey(tag: String, type: Class<out INBT>) = this.hasKey(tag, type.idForClass())
fun CompoundNBT?.hasKey(tag: String, id: Int) = this != null && this.contains(tag, id)
fun CompoundNBT?.hasUniqueId(tag: String) = this != null && this.hasUUID(tag)

fun CompoundNBT?.setBoolean(tag: String, value: Boolean) = this?.putBoolean(tag, value)
fun CompoundNBT?.setByte(tag: String, value: Byte) = this?.putByte(tag, value)
fun CompoundNBT?.setShort(tag: String, value: Short) = this?.putShort(tag, value)
fun CompoundNBT?.setInteger(tag: String, value: Int) = this?.putInt(tag, value)
fun CompoundNBT?.setIntArray(tag: String, value: IntArray) = this?.putIntArray(tag, value)
fun CompoundNBT?.setByteArray(tag: String, value: ByteArray) = this?.putByteArray(tag, value)
fun CompoundNBT?.setLong(tag: String, value: Long) = this?.putLong(tag, value)
fun CompoundNBT?.setFloat(tag: String, value: Float) = this?.putFloat(tag, value)
fun CompoundNBT?.setDouble(tag: String, value: Double) = this?.putDouble(tag, value)
fun CompoundNBT?.setCompoundTag(tag: String, value: CompoundNBT) = setTag(tag, value)
fun CompoundNBT?.setString(tag: String, value: String) = this?.putString(tag, value)
fun CompoundNBT?.setTagList(tag: String, value: ListNBT) = setTag(tag, value)
fun CompoundNBT?.setUniqueId(tag: String, value: UUID) = this?.putUUID(tag, value)
fun CompoundNBT?.setTag(tag: String, value: INBT) = this?.put(tag, value)

@JvmOverloads
fun CompoundNBT?.getBoolean(tag: String, defaultExpected: Boolean = false) = getIf(tag, CompoundNBT?::hasNumericKey, CompoundNBT::getBoolean, defaultExpected)

@JvmOverloads
fun CompoundNBT?.getByte(tag: String, defaultExpected: Byte = 0) = getIf(tag, CompoundNBT?::hasNumericKey, CompoundNBT::getByte, defaultExpected)

@JvmOverloads
fun CompoundNBT?.getShort(tag: String, defaultExpected: Short = 0) = getIf(tag, CompoundNBT?::hasNumericKey, CompoundNBT::getShort, defaultExpected)

@JvmOverloads
fun CompoundNBT?.getInteger(tag: String, defaultExpected: Int = 0) = getIf(tag, CompoundNBT?::hasNumericKey, CompoundNBT::getInt, defaultExpected)
fun CompoundNBT?.getIntArray(tag: String) = getIf(tag, CompoundNBT?::hasKey, CompoundNBT::getIntArray)
fun CompoundNBT?.getByteArray(tag: String) = getIf(tag, CompoundNBT?::hasKey, CompoundNBT::getByteArray)

@JvmOverloads
fun CompoundNBT?.getLong(tag: String, defaultExpected: Long = 0) = getIf(tag, CompoundNBT?::hasNumericKey, CompoundNBT::getLong, defaultExpected)

@JvmOverloads
fun CompoundNBT?.getFloat(tag: String, defaultExpected: Float = 0f) = getIf(tag, CompoundNBT?::hasNumericKey, CompoundNBT::getFloat, defaultExpected)

@JvmOverloads
fun CompoundNBT?.getDouble(tag: String, defaultExpected: Double = 0.0) = getIf(tag, CompoundNBT?::hasNumericKey, CompoundNBT::getDouble, defaultExpected)
fun CompoundNBT?.getCompoundTag(tag: String): CompoundNBT? = getIf(tag, CompoundNBT?::hasKey, CompoundNBT::getCompoundTag)
fun CompoundNBT?.getString(tag: String) = getIf(tag, CompoundNBT?::hasKey, CompoundNBT::getString)
fun CompoundNBT?.getTagList(tag: String, type: Class<out INBT>) = getTagList(tag, type.idForClass())
fun CompoundNBT?.getTagList(tag: String, objType: Int) = getIf(tag, CompoundNBT?::hasKey) { getList(it, objType) }
fun CompoundNBT?.getUniqueId(tag: String) = getIf(tag, CompoundNBT?::hasUniqueId, CompoundNBT::getUUID)
fun CompoundNBT?.getTag(tag: String) = getIf(tag, CompoundNBT?::hasKey, CompoundNBT::get)

// =========================================================================================================== ItemStack

fun ItemStack.removeNBTEntry(tag: String) = getTag().removeTag(tag)

fun ItemStack.hasNBTEntry(tag: String) = getTag().hasKey(tag)

@JvmName("setBoolean")
fun ItemStack.setNBTBoolean(tag: String, value: Boolean) = orCreateTag.setBoolean(tag, value)

@JvmName("setByte")
fun ItemStack.setNBTByte(tag: String, value: Byte) = orCreateTag.setByte(tag, value)

@JvmName("setShort")
fun ItemStack.setNBTShort(tag: String, value: Short) = orCreateTag.setShort(tag, value)

@JvmName("setInt")
fun ItemStack.setNBTInt(tag: String, value: Int) = orCreateTag.setInteger(tag, value)

@JvmName("setIntArray")
fun ItemStack.setNBTIntArray(tag: String, value: IntArray) = orCreateTag.setIntArray(tag, value)

@JvmName("setByteArray")
fun ItemStack.setNBTByteArray(tag: String, value: ByteArray) = orCreateTag.setByteArray(tag, value)

@JvmName("setLong")
fun ItemStack.setNBTLong(tag: String, value: Long) = orCreateTag.setLong(tag, value)

@JvmName("setFloat")
fun ItemStack.setNBTFloat(tag: String, value: Float) = orCreateTag.setFloat(tag, value)

@JvmName("setDouble")
fun ItemStack.setNBTDouble(tag: String, value: Double) = orCreateTag.setDouble(tag, value)

@JvmName("setCompound")
fun ItemStack.setNBTCompound(tag: String, value: CompoundNBT) = setNBTTag(tag, value)

@JvmName("setString")
fun ItemStack.setNBTString(tag: String, value: String) = orCreateTag.setString(tag, value)

@JvmName("setList")
fun ItemStack.setNBTList(tag: String, value: ListNBT) = setNBTTag(tag, value)

@JvmName("setUniqueId")
fun ItemStack.setNBTUniqueId(tag: String, value: UUID) = orCreateTag.setUniqueId(tag, value)

@JvmName("setTag")
fun ItemStack.setNBTTag(tag: String, value: INBT) = orCreateTag.setTag(tag, value)

@JvmOverloads
@JvmName("getBoolean")
fun ItemStack.getNBTBoolean(tag: String, defaultExpected: Boolean = false) = getTag().getBoolean(tag, defaultExpected)

@JvmOverloads
@JvmName("getByte")
fun ItemStack.getNBTByte(tag: String, defaultExpected: Byte = 0) = getTag().getByte(tag, defaultExpected)

@JvmOverloads
@JvmName("getShort")
fun ItemStack.getNBTShort(tag: String, defaultExpected: Short = 0) = getTag().getShort(tag, defaultExpected)

@JvmOverloads
@JvmName("getInt")
fun ItemStack.getNBTInt(tag: String, defaultExpected: Int = 0) = getTag().getInteger(tag, defaultExpected)

@JvmName("getIntArray")
fun ItemStack.getNBTIntArray(tag: String) = getTag().getIntArray(tag)

@JvmName("getByteArray")
fun ItemStack.getNBTByteArray(tag: String) = getTag().getByteArray(tag)

@JvmOverloads
@JvmName("getLong")
fun ItemStack.getNBTLong(tag: String, defaultExpected: Long = 0) = getTag().getLong(tag, defaultExpected)

@JvmOverloads
@JvmName("getFloat")
fun ItemStack.getNBTFloat(tag: String, defaultExpected: Float = 0f) = getTag().getFloat(tag, defaultExpected)

@JvmOverloads
@JvmName("getDouble")
fun ItemStack.getNBTDouble(tag: String, defaultExpected: Double = 0.0) = getTag().getDouble(tag, defaultExpected)

@JvmName("getCompound")
fun ItemStack.getNBTCompound(tag: String): CompoundNBT? = getTag().getCompoundTag(tag)

@JvmName("getString")
fun ItemStack.getNBTString(tag: String) = getTag().getString(tag)

@JvmName("getList")
fun ItemStack.getNBTList(tag: String, type: Class<out INBT>) = getNBTList(tag, type.idForClass())

@JvmName("getList")
fun ItemStack.getNBTList(tag: String, objType: Int) = getTag().getTagList(tag, objType)

@JvmName("getTag")
fun ItemStack.getNBTTag(tag: String) = getTag().getTag(tag)

// ========================================================================================================== Extensions

operator fun CompoundNBT?.contains(key: String) = hasKey(key)

// ListNBT ==========================================================================================================

val ListNBT.indices: IntRange
    get() = 0 until size

inline fun <reified T : INBT> ListNBT.forEach(run: (T) -> Unit) {
    for (tag in this)
        run(tag.castOrDefault())
}

inline fun <reified T : INBT> ListNBT.forEachIndexed(run: (Int, T) -> Unit) {
    for ((i, tag) in this.withIndex())
        run(i, tag.castOrDefault())
}

class NBTWrapper(private val contained: ItemStack) {
    operator fun set(s: String, tag: Any?) {
        if (tag == null) {
            contained.removeNBTEntry(s)
        } else contained.setNBTTag(s, convertNBT(tag)!!)
    }

    operator fun get(s: String): INBT? {
        return contained.getNBTTag(s)
    }
}

val ItemStack.nbt: NBTWrapper
    get() = NBTWrapper(this)

// CompoundNBT ======================================================================================================

operator fun CompoundNBT.iterator(): Iterator<Pair<String, INBT>> {
    return object : Iterator<Pair<String, INBT>> {
        val keys = this@iterator.allKeys.iterator()
        override fun hasNext() = keys.hasNext()
        override fun next(): Pair<String, INBT> {
            val next = keys.next()
            return next to this@iterator[next]!!
        }
    }
}

operator fun CompoundNBT.get(key: String): INBT? = this.getTag(key)

@JvmName("create")
fun tagCompound(lambda: NbtDsl.() -> Unit) = NbtDsl().apply(lambda).root

fun <T> list(vararg args: T): ListNBT {
    val list = ListNBT()
    args.forEach { list.add(convertNBT(it)) }
    return list
}

fun compound(vararg args: Pair<String, *>): CompoundNBT {
    val comp = CompoundNBT()
    args.forEach { convertNBT(it.second)?.let { it1 -> comp.setTag(it.first, it1) } }
    return comp
}

class NbtDsl(val root: CompoundNBT = CompoundNBT()) {
    operator fun String.invoke(lambda: NbtDsl.() -> Unit) {
        root[this] = tagCompound(lambda)
    }

    infix fun String.to(lambda: NbtDsl.() -> Unit) = this(lambda)

    @JvmName("append")
    operator fun String.invoke(lambda: Consumer<NbtDsl>) = this { lambda.accept(this) }

    @JvmName("append")
    operator fun String.invoke(vararg values: Any?) {
        root[this] = if (values.size == 1) convertNBT(values.first())!! else convertNBT(values)!!
    }

    infix fun String.to(value: Any?) = this(value)
}

operator fun CompoundNBT.set(key: String, value: INBT) = setTag(key, value)

fun convertNBT(value: Any?): INBT? = when (value) {
    is INBT -> value

    null -> ByteNBT.valueOf(0)
    is Boolean -> ByteNBT.valueOf(if (value) 1 else 0)
    is Byte -> ByteNBT.valueOf(value)
    is Char -> ShortNBT.valueOf(value.code.toShort())
    is Short -> ShortNBT.valueOf(value)
    is Int -> IntNBT.valueOf(value)
    is Long -> LongNBT.valueOf(value)
    is Float -> FloatNBT.valueOf(value)
    is Double -> DoubleNBT.valueOf(value)
    is ByteArray -> ByteArrayNBT(value)
    is String -> StringNBT.valueOf(value)
    is IntArray -> IntArrayNBT(value)
    is UUID -> ListNBT().apply {
        add(LongNBT.valueOf(value.leastSignificantBits))
        add(LongNBT.valueOf(value.mostSignificantBits))
    }
    is Array<*> -> list(*value)
    is Collection<*> -> list(*value.toTypedArray())
    is Map<*, *> -> compound(*value.toList().map { it.first.toString() to it.second }.toTypedArray())
    is ResourceLocation -> StringNBT.valueOf(value.toString())
    is INBTSerializable<*> -> value.serializeNBT()
    is IStringSerializable -> StringNBT.valueOf(value.serializedName)

    else -> null
}
