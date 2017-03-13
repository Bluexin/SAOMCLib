package be.bluexin.saomclib.capabilities

import net.minecraft.entity.Entity
import net.minecraftforge.common.IExtendedEntityProperties

/**
 * Superclass for all capabilities to be registered with this library.
 *
 * For it to work properly, implementations should declare the following :
 *
 *  - A static field holding a [net.minecraft.util.ResourceLocation] annotated with [Key]
 *          ie: @Key private static final ResourceLocation myKey = new ResourceLocation(MODID, "myCapability")
 *
 * Please note: In 1.7.10 only Entities can get Capabilities. I did not bother to correct all of the doc.
 * However, please extend [AbstractEntityCapability] instead of this class.
 *
 * @author Bluexin
 */
abstract class AbstractCapability : IExtendedEntityProperties {

    /**
     * This should be where the setting up of the capability happens.
     * It will always only be of the type for which you registered this capability.
     * For example, if I register my capability with [CapabilitiesHandler.registerEntityCapability], then
     * this method will always receive a subtype of Entity
     *
     * @return should return this
     * @see [IExtendedEntityProperties.init]
     */
    abstract fun setup(param: Entity): AbstractCapability
}
