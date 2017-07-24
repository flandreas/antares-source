package ch.scorpion.jabbah.base

import kotlin.reflect.KClass

/**
 * Defines a simple API for a logging system that is implemented on the various supported platforms.
 *
 * Usage pattern:
 *
 *      class Something {
 *          private val LOG by logger(Something::class)
 *
 *          fun foo() {
 *              LOG.info("Hello from Something")
 *          }
 *      }
 */

interface LogSystem {

    /** Returns the [Logger] to be used for a particular object. */
    fun getLogger(clazz: KClass<out Any>): Lazy<Logger>
}

var LOG_SYSTEM: LogSystem? = null

interface Logger {
    fun info(msg: String)
    fun warn(msg: String)
    fun error(msg: String)
    fun debug(msg: String)
    fun trace(msg: String)
    fun isDebugEnabled(): Boolean
    fun isTraceEnabled(): Boolean

    fun setLogLevel(level: LogLevel?)
}

enum class LogLevel {
    NONE,
    ERROR,
    WARN,
    INFO,
    DEBUG,
    TRACE
}

fun <T: Any> logger(origin: KClass<T>): Lazy<Logger> {
    return LOG_SYSTEM!!.getLogger(origin)
}



