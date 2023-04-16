@file:Suppress("KDocUnresolvedReference")

package com.tencao.saomclib.except

import com.tencao.saomclib.capabilities.AbstractCapability
import net.minecraft.util.ResourceLocation

/**
 * Supertype of all exceptions linked to Capabilities.

 * @author Bluexin
 */
abstract class CapabilityException : RuntimeException {
    constructor(message: String) : super(message)

    constructor(message: String, cause: Throwable) : super(message, cause)
}

/**
 * Thrown when a field or method was found with the wrong type.
 */
class WrongTypeException(clazz: Class<out AbstractCapability>, what: String, expected: Class<*>, cause: ClassCastException) : CapabilityException("The $what found for $clazz was not an instance of $expected (or has wrong generic type).", cause)

/**
 * Thrown when an expected field or method was not found.
 */
class NoPresentException(clazz: Class<out AbstractCapability>, what: String) : CapabilityException("No $what found for $clazz.")

/**
 * Thrown when multiple fields or methods were found for the same annotation.
 */
class DuplicateException(clazz: Class<*>, what: String) : CapabilityException("Found duplicate $what in $clazz.")

/**
 * Thrown when a field or method was found to be non-static.
 */
class NotStaticException(clazz: Class<out AbstractCapability>, what: String) : CapabilityException("The $what found for $clazz was not static.")

/**
 * Thrown when an unknown exception occurs.
 */
class UnknownCapabilityException(clazz: Class<out AbstractCapability>, cause: Throwable) : CapabilityException("An unknown error occurred when evaluating $clazz.", cause)

/**
 * Thrown when a mod tries to register a capability during the wrong phase.
 */
class WrongPhaseException(clazz: Class<out AbstractCapability>) : CapabilityException("Tried to register $clazz as capability after the lib invoked init()!")

/**
 * Thrown when a mod tries to find a capability based on an unknown [id]
 */
class IDNotFoundException(id: ResourceLocation) : CapabilityException("No capability found for ID $id.")

/**
 * Thrown when a mod tries to register a capability with an already existing ID.
 */
class IDNotUniqueException(clazz: Class<out AbstractCapability>, id: Long, owner: Class<out AbstractCapability>) : CapabilityException("Tried to register $clazz with non-unique ID $id owned by $owner.")
