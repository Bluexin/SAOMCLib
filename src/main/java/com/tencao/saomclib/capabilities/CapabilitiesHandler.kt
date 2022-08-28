package com.tencao.saomclib.capabilities

import com.tencao.saomclib.except.*
import net.minecraft.entity.Entity
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTBase
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.EnumFacing
import net.minecraft.util.ResourceLocation
import net.minecraft.world.World
import net.minecraft.world.chunk.Chunk
import net.minecraftforge.common.capabilities.*
import net.minecraftforge.event.AttachCapabilitiesEvent

/**
 * Handles all the registering of capabilities.
 * @see [AbstractCapability] for more info.
 *
 * @author Bluexin
 */
@Suppress("unused")
object CapabilitiesHandler {

    private var entitiez: ArrayList<Pair<Class<out AbstractEntityCapability>, (Any) -> Boolean>>? = ArrayList()
    private var entitiezz: HashMap<ResourceLocation, CapabilityStorage>? = null
    private var tileEntitiez: ArrayList<Pair<Class<out AbstractCapability>, (Any) -> Boolean>>? = ArrayList()
    private var tileEntitiezz: HashMap<ResourceLocation, CapabilityStorage>? = null
    private var itemz: ArrayList<Pair<Class<out AbstractCapability>, (Any) -> Boolean>>? = ArrayList()
    private var itemzz: HashMap<ResourceLocation, CapabilityStorage>? = null
    private var worldz: ArrayList<Pair<Class<out AbstractCapability>, (Any) -> Boolean>>? = ArrayList()
    private var worldzz: HashMap<ResourceLocation, CapabilityStorage>? = null
    private var chunkz: ArrayList<Pair<Class<out AbstractCapability>, (Any) -> Boolean>>? = ArrayList()
    private var chunkzz: HashMap<ResourceLocation, CapabilityStorage>? = null
    // TODO: add Village & Chunk

    /**
     * Register a capability to registry for all subtypes of Entity.
     */
    fun <T : AbstractEntityCapability> registerEntityCapability(clazz: Class<T>, storage: Capability.IStorage<T>, assignable: (Any) -> Boolean) {
        entitiez?.add(Pair(clazz, assignable)) ?: throw WrongPhaseException(clazz)
        CapabilityManager.INSTANCE.register(clazz, storage, clazz)
    }

    /**
     * Register a capability to registry for all subtypes of ItemStack.
     */
    fun <T : AbstractCapability> registerItemCapability(clazz: Class<T>, storage: Capability.IStorage<T>, assignable: (Any) -> Boolean) {
        itemz?.add(Pair(clazz, assignable)) ?: throw WrongPhaseException(clazz)
        CapabilityManager.INSTANCE.register(clazz, storage, clazz)
    }

    /**
     * Register a capability to registry for all subtypes of TileEntity.
     */
    fun <T : AbstractCapability> registerTECapability(clazz: Class<T>, storage: Capability.IStorage<T>, assignable: (Any) -> Boolean) {
        tileEntitiez?.add(Pair(clazz, assignable)) ?: throw WrongPhaseException(clazz)
        CapabilityManager.INSTANCE.register(clazz, storage, clazz)
    }

    /**
     * Register a capability to registry for all subtypes of World.
     */
    fun <T : AbstractCapability> registerWorldCapability(clazz: Class<T>, storage: Capability.IStorage<T>, assignable: (Any) -> Boolean) {
        worldz?.add(Pair(clazz, assignable)) ?: throw WrongPhaseException(clazz)
        CapabilityManager.INSTANCE.register(clazz, storage, clazz)
    }

    /**
     * Register a capability to registry for all subtypes of Chunk.
     */
    fun <T : AbstractCapability> registerChunkCapability(clazz: Class<T>, storage: Capability.IStorage<T>, assignable: (Any) -> Boolean) {
        chunkz?.add(Pair(clazz, assignable)) ?: throw WrongPhaseException(clazz)
        CapabilityManager.INSTANCE.register(clazz, storage, clazz)
    }

    /**
     * Queries a Entity Capability based on it's [id], or throws an [IDNotFoundException] if no capability was found.
     */
    fun getEntityCapability(id: ResourceLocation) = entitiezz!![id]?.capability ?: throw IDNotFoundException(id)

    /**
     * Queries a ItemStack Capability based on it's ID, or throws an [IDNotFoundException] if no capability was found.
     */
    fun getItemCapability(id: ResourceLocation) = itemzz!![id]?.capability ?: throw IDNotFoundException(id)

    /**
     * Queries a TE Capability based on it's ID, or throws an [IDNotFoundException] if no capability was found.
     */
    fun getTileEntityCapability(id: ResourceLocation) = tileEntitiezz!![id]?.capability ?: throw IDNotFoundException(id)

    /**
     * Queries a World Capability based on it's ID, or throws an [IDNotFoundException] if no capability was found.
     */
    fun getWorldCapability(id: ResourceLocation) = worldzz!![id]?.capability ?: throw IDNotFoundException(id)

    /**
     * Queries a Chunk Capability based on it's ID, or throws an [IDNotFoundException] if no capability was found.
     */
    fun getChunkCapability(id: ResourceLocation) = chunkzz!![id]?.capability ?: throw IDNotFoundException(id)

    /**
     * Gets the ID of a Capability.
     */
    fun getID(clazz: Class<out AbstractCapability>) = getKey(clazz)

    @Throws(CapabilityException::class)
    internal fun setup() {
        entitiezz = HashMap(entitiez?.size ?: 0, 1F)
        entitiez?.map { CapabilityStorage(getKey(it.first), getCapability(it.first), it.first, it.second) }?.forEach { entitiezz!![getID(it.clazz)] = it }
        entitiez = null
        itemzz = HashMap(itemz?.size ?: 0, 1F)
        itemz?.map { CapabilityStorage(getKey(it.first), getCapability(it.first), it.first, it.second) }?.forEach { itemzz!![getID(it.clazz)] = it }
        itemz = null
        tileEntitiezz = HashMap(tileEntitiez?.size ?: 0, 1F)
        tileEntitiez?.map { CapabilityStorage(getKey(it.first), getCapability(it.first), it.first, it.second) }?.forEach { tileEntitiezz!![getID(it.clazz)] = it }
        tileEntitiez = null
        worldzz = HashMap(worldz?.size ?: 0, 1F)
        worldz?.map { CapabilityStorage(getKey(it.first), getCapability(it.first), it.first, it.second) }?.forEach { worldzz!![getID(it.clazz)] = it }
        worldz = null
        chunkzz = HashMap(chunkz?.size ?: 0, 1F)
        chunkz?.map { CapabilityStorage(getKey(it.first), getCapability(it.first), it.first, it.second) }?.forEach { chunkzz!![getID(it.clazz)] = it }
        chunkz = null
    }

    internal fun syncEntitiesDeath(entity: Entity) = entitiezz
        ?.filter { it.value.isAssignable(entity) && (entity.getCapability(it.value.capability, null) as AbstractEntityCapability).shouldSyncOnDeath }
        ?.forEach {
            (entity.getCapability(it.value.capability, null) as AbstractEntityCapability).sync()
        }

    internal fun restoreEntitiesDeath(entity: Entity, original: Entity) = entitiezz
        ?.filter { it.value.isAssignable(entity) && (entity.getCapability(it.value.capability, null) as AbstractEntityCapability).shouldRestoreOnDeath }
        ?.forEach {
            if ((entity.getCapability(it.value.capability, null) as AbstractEntityCapability).restore(entity, original)) it.value.capability.readNBT(entity.getCapability(it.value.capability, null), null, it.value.capability.writeNBT(original.getCapability(it.value.capability, null), null))
        }

    internal fun syncEntitiesDimension(entity: Entity) = entitiezz
        ?.filter { it.value.isAssignable(entity) && (entity.getCapability(it.value.capability, null) as AbstractEntityCapability).shouldSyncOnDimensionChange }
        ?.forEach {
            val c = (entity.getCapability(it.value.capability, null) as AbstractEntityCapability)
            if (c.restore(entity, entity)) c.sync()
        }

    internal fun syncEntitiesLogin(entity: Entity) =
        entitiezz
            ?.filter { it.value.isAssignable(entity) && (entity.getCapability(it.value.capability, null) as AbstractEntityCapability).shouldSendOnLogin }
            ?.forEach { (entity.getCapability(it.value.capability, null) as AbstractEntityCapability).sync() }

    internal fun registerEntity(event: AttachCapabilitiesEvent<Entity>) = entitiezz?.filter { it.value.shouldRegister(event.`object`) }?.forEach { event.addCapability(it.value.key, CapabilitySerializableImpl(it.value.clazz, it.value.capability, event.`object`)) }

    internal fun registerItem(event: AttachCapabilitiesEvent<ItemStack>) = itemzz?.filter { it.value.shouldRegister(event.`object`) }?.forEach { event.addCapability(it.value.key, CapabilitySerializableImpl(it.value.clazz, it.value.capability, event.`object`)) }

    internal fun registerTE(event: AttachCapabilitiesEvent<TileEntity>) = tileEntitiezz?.filter { it.value.shouldRegister(event.`object`) }?.forEach { event.addCapability(it.value.key, CapabilitySerializableImpl(it.value.clazz, it.value.capability, event.`object`)) }

    internal fun registerWorld(event: AttachCapabilitiesEvent<World>) = worldzz?.filter { it.value.shouldRegister(event.`object`) }?.forEach { event.addCapability(it.value.key, CapabilitySerializableImpl(it.value.clazz, it.value.capability, event.`object`)) }

    internal fun registerChunk(event: AttachCapabilitiesEvent<Chunk>) = chunkzz?.filter { it.value.shouldRegister(event.`object`) }?.forEach { event.addCapability(it.value.key, CapabilitySerializableImpl(it.value.clazz, it.value.capability, event.`object`)) }

    private fun getKey(clazz: Class<out AbstractCapability>) = try {
        clazz.declaredFields.single { it.isAnnotationPresent(Key::class.java) }.apply { this.isAccessible = true }.get(null) as ResourceLocation
    } catch (e: ClassCastException) {
        throw WrongTypeException(clazz, "Key", Key::class.java, e)
    } catch (e: NullPointerException) {
        throw NotStaticException(clazz, "Key")
    } catch (e: NoSuchElementException) {
        throw NoPresentException(clazz, "Key")
    } catch (e: IllegalArgumentException) {
        throw DuplicateException(clazz, "Key")
    } catch (e: Exception) {
        throw UnknownCapabilityException(clazz, e) // Should not happen
    }

    private fun <T : AbstractCapability> getCapability(clazz: Class<out T>) = try {
        @Suppress("UNCHECKED_CAST")
        clazz.declaredFields.single { it.isAnnotationPresent(CapabilityInject::class.java) }.apply { this.isAccessible = true }.get(null) as Capability<T>
    } catch (e: ClassCastException) {
        throw WrongTypeException(clazz, "capability instance", CapabilityInject::class.java, e)
    } catch (e: NullPointerException) {
        throw NotStaticException(clazz, "capability instance")
    } catch (e: NoSuchElementException) {
        throw NoPresentException(clazz, "capability instance")
    } catch (e: IllegalArgumentException) {
        throw DuplicateException(clazz, "capability instance")
    } catch (e: Exception) {
        throw UnknownCapabilityException(clazz, e) // Should not happen
    }

    internal fun <T : AbstractCapability> getInstance(clazz: Class<out T>, arg: Any): T {
        @Suppress("UNCHECKED_CAST")
        return clazz.newInstance().setup(arg) as T
    }

    private class CapabilitySerializableImpl<T : AbstractCapability>(private val clazz: Class<out T>, private val capability: Capability<T>, arg: Any) : ICapabilitySerializable<NBTBase> {
        private val instance: T = getInstance(this.clazz, arg)

        override fun hasCapability(capability: Capability<*>, facing: EnumFacing?) = capability === this.capability

        override fun <R> getCapability(capability: Capability<R>, facing: EnumFacing?) = if (capability === this.capability) this.capability.cast<R>(instance) else null

        override fun serializeNBT(): NBTBase = this.capability.storage.writeNBT(this.capability, this.instance, null)
            ?: NBTTagCompound()

        override fun deserializeNBT(nbt: NBTBase) = this.capability.storage.readNBT(this.capability, this.instance, null, nbt)
    }

    internal fun getEntityCapabilityImpl(id: ResourceLocation) = entitiezz!![id] ?: throw IDNotFoundException(id)

    internal fun getItemCapabilityImpl(id: ResourceLocation) = itemzz!![id] ?: throw IDNotFoundException(id)

    internal fun getTileEntityCapabilityImpl(id: ResourceLocation) = tileEntitiezz!![id]
        ?: throw IDNotFoundException(id)

    internal fun getWorldCapabilityImpl(id: ResourceLocation) = worldzz!![id] ?: throw IDNotFoundException(id)

    internal fun getChunkCapabilityImpl(id: ResourceLocation) = chunkzz!![id] ?: throw IDNotFoundException(id)

    internal data class CapabilityStorage(val key: ResourceLocation, val capability: Capability<AbstractCapability>, val clazz: Class<out AbstractCapability>, private val assignable: (Any) -> Boolean) {
        fun shouldRegister(arg: Any) = isAssignable(arg) && !(arg as ICapabilityProvider).hasCapability(capability, null)
        fun isAssignable(arg: Any) = assignable.invoke(arg)
    }
}
