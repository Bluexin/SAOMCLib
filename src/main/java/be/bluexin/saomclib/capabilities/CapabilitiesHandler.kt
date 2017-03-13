package be.bluexin.saomclib.capabilities

import be.bluexin.saomclib.except.*
import net.minecraft.entity.Entity
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.ResourceLocation
import net.minecraftforge.event.entity.EntityEvent
import java.util.*

@Suppress("unused")
/**
 * Handles all the registering of capabilities.
 * @see [AbstractCapability] for more info.
 *
 * @author Bluexin
 */
object CapabilitiesHandler {

    private var entitiez: ArrayList<Pair<Class<out AbstractEntityCapability>, (Entity) -> Boolean>>? = ArrayList()
    private var entitiezz: HashMap<ResourceLocation, CapabilityStorage>? = null

    /**
     * Register a capability to registry for all subtypes of Entity.
     */
    fun <T : AbstractEntityCapability> registerEntityCapability(clazz: Class<T>, assignable: (Entity) -> Boolean) {
        entitiez?.add(Pair(clazz, assignable)) ?: throw WrongPhaseException(clazz)
    }

    /**
     * Gets the ID of a Capability.
     */
    fun getID(clazz: Class<out AbstractCapability>) = getKey(clazz)

    @Throws(CapabilityException::class)
    internal fun setup() {
        entitiezz = HashMap(entitiez?.size ?: 0, 1F)
        entitiez?.map { CapabilityStorage(getKey(it.first), it.first, it.second) }?.forEach { entitiezz!!.put(getID(it.clazz), it) }
        entitiez = null
    }

    internal fun syncEntitiesDeath(entity: Entity) = entitiezz?.filter { it.value.isAssignable(entity) && (entity.getExtendedProperties(it.key.toString()) as AbstractEntityCapability).shouldSyncOnDeath }?.forEach { (entity.getExtendedProperties(it.key.toString()) as AbstractEntityCapability).sync() }

    internal fun restoreEntitiesDeath(entity: Entity, original: Entity) = entitiezz?.filter { it.value.isAssignable(entity) && (entity.getExtendedProperties(it.key.toString()) as AbstractEntityCapability).shouldRestoreOnDeath }?.forEach {
        val tag = NBTTagCompound()
        original.getExtendedProperties(it.key.toString()).saveNBTData(tag)
        entity.getExtendedProperties(it.key.toString()).loadNBTData(tag)
    }

    internal fun syncEntitiesDimension(entity: Entity) = entitiezz?.filter { it.value.isAssignable(entity) && (entity.getExtendedProperties(it.key.toString()) as AbstractEntityCapability).shouldSyncOnDimensionChange }?.forEach { (entity.getExtendedProperties(it.key.toString()) as AbstractEntityCapability).sync() }

    internal fun syncEntitiesLogin(entity: Entity) = entitiezz?.filter { it.value.isAssignable(entity) && (entity.getExtendedProperties(it.key.toString()) as AbstractEntityCapability).shouldSendOnLogin }?.forEach { (entity.getExtendedProperties(it.key.toString()) as AbstractEntityCapability).sync() }

    internal fun registerEntity(event: EntityEvent.EntityConstructing) = entitiezz?.filter {
        it.value.shouldRegister(event.entity)
    }?.forEach {
        event.entity.registerExtendedProperties(it.key.toString(), getInstance(it.value.clazz, event.entity))
    }

    private fun getKey(clazz: Class<out AbstractCapability>) = try {
        clazz.declaredFields.filter { it.isAnnotationPresent(Key::class.java) }.single().apply { this.isAccessible = true }.get(null) as ResourceLocation
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

    internal fun <T : AbstractCapability> getInstance(clazz: Class<out T>, arg: Entity): T {
        @Suppress("UNCHECKED_CAST")
        return clazz.newInstance().setup(arg) as T
    }

    internal fun getEntityCapabilityImpl(id: ResourceLocation) = entitiezz!![id] ?: throw IDNotFoundException(id)

    internal data class CapabilityStorage(val key: ResourceLocation, val clazz: Class<out AbstractCapability>, private val assignable: (Entity) -> Boolean) {
        fun shouldRegister(arg: Entity) = isAssignable(arg) && arg.getExtendedProperties(key.toString()) == null
        fun isAssignable(arg: Entity) = assignable.invoke(arg)
    }
}
