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

expect object LogSystem {

	val PROP_LOG_LEVEL: String

	/** The root log level of this [LogSystem].*/
	var level: LogLevel

	/** Returns the [Logger] to be used for a particular object. */
    fun getLogger(clazz: KClass<out Any>): Lazy<Logger>
}

expect class Logger {

	/** Used for a few important information like statistics etc.*/
	fun info(msg: String)

	/** Used for warnings like configuration errors etc. */
    fun warn(msg: String)

    /** Used for errors like unexpected exceptions etc. */
    fun error(msg: String, t: Throwable? = null)

    /** Used as a trail of all relevant user actions. Primarily used by testers to produce test action protocols. */
    fun debug(msg: String)

    /** Used for detailed information. Primarily used by the developer to investigate individual issues. */
    fun trace(msg: String)

    fun isDebugEnabled(): Boolean
    fun isTraceEnabled(): Boolean
}

enum class LogLevel {
    Error,
    Info,
    Warning,
    Debug,
    Trace
}

fun <T: Any> logger(origin: KClass<T>): Lazy<Logger> = LogSystem.getLogger(origin)