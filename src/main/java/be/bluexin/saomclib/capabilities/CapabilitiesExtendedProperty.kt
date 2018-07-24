package be.bluexin.saomclib.capabilities

import com.google.common.base.Preconditions
import com.google.common.collect.Lists
import cpw.mods.fml.common.FMLLog
import cpw.mods.fml.common.discovery.ASMDataTable
import net.minecraft.entity.Entity
import net.minecraft.nbt.NBTBase
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.EnumFacing
import net.minecraft.util.ResourceLocation
import net.minecraft.world.World
import net.minecraftforge.common.IExtendedEntityProperties
import net.minecraftforge.common.util.EnumHelper
import org.apache.logging.log4j.Level
import org.objectweb.asm.Type
import java.lang.reflect.Modifier
import java.util.*
import kotlin.reflect.KClass

/**
 * Compatibility layer emulating a subset of Capabilities in 1.7.10 (but limited to Entities).
 * Most of the code has been heavily inspired by (or copied over from) Forge's implementation on 1.12.2.
 */
class CapabilitiesExtendedProperty : IExtendedEntityProperties {

    private val capabilities = mutableMapOf<ResourceLocation, ICapabilitySerializable<NBTBase>>()

    override fun loadNBTData(compound: NBTTagCompound) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun saveNBTData(compound: NBTTagCompound) {
        capabilities.forEach {(key, provider) ->
            compound.setTag(key.toString(), provider.serializeNBT())
        }
    }

    override fun init(entity: Entity, world: World) {
        CapabilitiesHandler
    }

    operator fun get(capability: Capability<*>, facing: EnumFacing?) =
            capabilities.values.firstOrNull { it.hasCapability(capability, facing) }?.getCapability(capability, facing)

    operator fun get(key: ResourceLocation) = capabilities[key]
    operator fun set(key: ResourceLocation, provider: ICapabilitySerializable<NBTBase>) {
        capabilities[key] = provider
    }

    companion object {
        val KEY = ResourceLocation("saomclib", "capabilities").toString()
    }
}

// String name, IStorage<T> storage, Callable<? extends T> factory
class Capability<T> internal constructor(
        val name: String,
        val storage: IStorage<T>,
        val factory: () -> T
) {
    @Suppress("UNCHECKED_CAST")
    fun <R> cast(instance: T): R {
        return instance as R
    }

    fun readNBT(instance: T, side: EnumFacing?, nbt: NBTBase) = storage.readNBT(this, instance, side, nbt)

    fun writeNBT(instance: T, side: EnumFacing?): NBTBase = storage.writeNBT(this, instance, side)

    interface IStorage<T> {
        fun writeNBT(capability: Capability<T>, instance: T, side: EnumFacing?): NBTBase

        fun readNBT(capability: Capability<T>, instance: T, side: EnumFacing?, nbt: NBTBase)
    }
}

val Entity.capabilities
    get() = this.getExtendedProperties(CapabilitiesExtendedProperty.KEY) as CapabilitiesExtendedProperty

fun Entity.hasCapability(capability: Capability<*>, facing: EnumFacing?): Boolean {
    TODO()
}

fun <T> Entity.getCapability(capability: Capability<T>, facing: EnumFacing?): T? {
    TODO()
}

fun Entity.addCapability(key: ResourceLocation, provider: ICapabilitySerializable<NBTBase>) {
    this.capabilities[key] = provider
}

interface ICapabilitySerializable<T: NBTBase> {
    fun hasCapability(capability: Capability<*>, facing: EnumFacing?): Boolean

    fun <R> getCapability(capability: Capability<R>, facing: EnumFacing?): R?

    fun serializeNBT(): T

    fun deserializeNBT(nbt: T)
}

object CapabilityManager {

    @Deprecated("use ::new factory instead", replaceWith = ReplaceWith("CapabilityManager.register(type, storage, implementation::newInstance)"))
    fun <T> register(type: Class<T>, storage: Capability.IStorage<T>, implementation: Class<out T>) {
        register(type, storage, implementation::newInstance)
    }

    /**
     * Registers a capability to be consumed by others.
     * APIs who define the capability should call this.
     * To retrieve the Capability instance, use the @CapabilityInject annotation.
     *
     * @param type The Interface to be registered
     * @param storage A default implementation of the storage handler.
     * @param factory A Factory that will produce new instances of the default implementation.
     */
    fun <T> register(type: Class<T>, storage: Capability.IStorage<T>, factory: () -> T) {
        val realName = type.name.intern()
        Preconditions.checkState(!providers.containsKey(realName), "Can not register a capability implementation multiple times: %s", realName)

        val cap = Capability(realName, storage, factory)
        providers[realName] = cap

        val list = callbacks[realName]
        if (list != null) {
            for (func in list) {
                func(cap)
            }
        }
    }

    private val providers = IdentityHashMap<String, Capability<*>>()
    private val callbacks = IdentityHashMap<String, MutableList<(Capability<*>) -> Any?>>()
    internal fun injectCapabilities(data: ASMDataTable) {
        for (entry in data.getAll(CapabilityInject::class.java.name)) {
            val targetClass = entry.className
            val targetName = entry.objectName
            val type = entry.annotationInfo["value"] as Type?
            if (type == null) {
                FMLLog.log(Level.WARN, "Unable to inject capability at {}.{} (Invalid Annotation)", targetClass, targetName)
                continue
            }
            val capabilityName = type.internalName.replace('/', '.').intern()

            val list = callbacks.computeIfAbsent(capabilityName) { _ -> Lists.newArrayList() }

            if (entry.objectName.indexOf('(') > 0) {
                list.add { input ->
                    try {
                        for (mtd in Class.forName(targetClass).declaredMethods) {
                            if (targetName == mtd.name + Type.getMethodDescriptor(mtd)) {
                                if (mtd.modifiers and Modifier.STATIC != Modifier.STATIC) {
                                    FMLLog.log(Level.WARN, "Unable to inject capability {} at {}.{} (Non-Static)", capabilityName, targetClass, targetName)
                                    return@add null
                                }

                                mtd.isAccessible = true
                                mtd.invoke(null, input)
                                return@add null
                            }
                        }
                        FMLLog.log(Level.WARN, "Unable to inject capability {} at {}.{} (Method Not Found)", capabilityName, targetClass, targetName)
                    } catch (e: Exception) {
                        FMLLog.log(Level.WARN, "Unable to inject capability {} at {}.{}", capabilityName, targetClass, targetName, e)
                    }

                    null
                }
            } else {
                list.add { input ->
                    try {
                        val field = Class.forName(targetClass).getDeclaredField(targetName)
                        if (field.modifiers and Modifier.STATIC != Modifier.STATIC) {
                            FMLLog.log(Level.WARN, "Unable to inject capability {} at {}.{} (Non-Static)", capabilityName, targetClass, targetName)
                            return@add null
                        }
                        EnumHelper.setFailsafeFieldValue(field, null, input)
                    } catch (e: Exception) {
                        FMLLog.log(Level.WARN, "Unable to inject capability {} at {}.{}", capabilityName, targetClass, targetName, e)
                    }

                    null
                }
            }
        }
    }
}

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD, AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY_SETTER)
annotation class CapabilityInject(val value: KClass<*>)