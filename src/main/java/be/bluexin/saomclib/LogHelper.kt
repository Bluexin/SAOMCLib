package be.bluexin.saomclib

import org.apache.logging.log4j.Level
import org.apache.logging.log4j.LogManager
import java.io.PrintWriter
import java.io.StringWriter

@Suppress("unused")
internal object LogHelper {

    private val logger = LogManager.getLogger(SAOMCLib.MODID)

    fun log(level: Level, msg: String) = logger.log(level, msg)

    fun logInfo(msg: String) = logger.info(msg)

    fun logWarn(msg: String) = logger.warn(msg)

    fun logFatal(msg: String) = logger.fatal(msg)

    fun log(e: Throwable) {
        val w = StringWriter()
        e.printStackTrace(PrintWriter(w))
        logFatal(w.toString())
    }
}
/*
[public be.bluexin.saomclib.capabilities.AbstractCapability be.bluexin.saomclib.example.SimpleCapability.setup(java.lang.Object),
public be.bluexin.saomclib.example.SimpleCapability be.bluexin.saomclib.example.SimpleCapability.setup(java.lang.Object),
public final void be.bluexin.saomclib.example.SimpleCapability.setNum(int),
public final int be.bluexin.saomclib.example.SimpleCapability.getNum(),
public static final net.minecraft.util.ResourceLocation be.bluexin.saomclib.example.SimpleCapability.access$getKEY$cp(),
public final void java.lang.Object.wait() throws java.lang.InterruptedException,
public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException,
public final native void java.lang.Object.wait(long) throws java.lang.InterruptedException,
public boolean java.lang.Object.equals(java.lang.Object),public java.lang.String java.lang.Object.toString(),
public native int java.lang.Object.hashCode(),public final native java.lang.Class java.lang.Object.getClass(),
public final native void java.lang.Object.notify(),public final native void java.lang.Object.notifyAll()]

 */
