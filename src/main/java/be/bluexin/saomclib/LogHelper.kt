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

    fun logDebug(msg: String) = logger.debug(msg)

    fun log(e: Throwable) {
        val w = StringWriter()
        e.printStackTrace(PrintWriter(w))
        logFatal(w.toString())
    }
}
