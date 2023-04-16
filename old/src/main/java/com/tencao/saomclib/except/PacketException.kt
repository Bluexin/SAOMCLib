package com.tencao.saomclib.except

import net.minecraftforge.fml.common.network.simpleimpl.IMessage

/**
 * Supertype of all exceptions linked to Packets.
 *
 * @author Bluexin
 */
abstract class PacketException : RuntimeException {
    constructor(message: String) : super(message)

    constructor(message: String, cause: Throwable) : super(message, cause)
}

/**
 * Thrown when a [IMessage] is registered without having a zero-parameter constructor.
 * Forge silently fails this, leading to hard to track down errors.
 */
class NoConstructorException(clazz: Class<out IMessage>) : PacketException("$clazz appears to be missing a zero-parameter argument!")

/**
 * Thrown when an unknown exception occurs.
 */
class UnknownPacketException(clazz: Class<out IMessage>, cause: Throwable) : PacketException("An unknown error occurred when evaluating $clazz.", cause)
