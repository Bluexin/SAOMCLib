package be.bluexin.saomclib.capabilities

/**
 * Superclass for all capabilities to be registered with this library.
 *
 * For it to work properly, implementations should declare the following :
 *
 *  - A static field to hold the instance of Capability and marked with [net.minecraftforge.common.capabilities.CapabilityInject]
 *          ie: @CapabilityInject(MyCapability.class) public static Capability<MyCapability> myCapability
 *          @see net.minecraftforge.common.capabilities.CapabilityInject documentation for more information.
 *
 *  - A static field holding a [net.minecraft.util.ResourceLocation] annotated with [Key]
 *          ie: @Key private static final ResourceLocation myKey = new ResourceLocation(MODID, "myCapability")
 *
 *  - A static method named 'isAssignable' with a boolean as return value and with a single parameter of type Object
 *          whose goal will be to determine whether a given argument can be assigned this capability.
 *
 *              Whether this capability should be assigned to a given Object.
 *              Given Object will always be a subclass of what's supported by [net.minecraftforge.event.AttachCapabilitiesEvent].
 *              It will always only be of the type for which you registered this capability.
 *              For example, if I register my capability with [CapabilitiesHandler.registerEntityCapability], then
 *              this method will only be called for subtypes of Entity.
 *          ie: private static boolean isAssignable(Object o) { return o instanceof EntityLivingBase; }
 *
 * Please note that this implementation currently doesn't allow for an [net.minecraft.util.EnumFacing] specification in TEs.
 *
 * @author Bluexin
 */
abstract class AbstractCapability {

    /**
     * This should be where the setting up of the capability happens.
     * The parameter will always be a subclass of what's supported by [net.minecraftforge.event.AttachCapabilitiesEvent].
     * It will always only be of the type for which you registered this capability.
     * For example, if I register my capability with [CapabilitiesHandler.registerEntityCapability], then
     * this method will always receive a subtype of Entity
     *
     * @return should return this
     */
    abstract fun setup(param: Any): AbstractCapability
}
