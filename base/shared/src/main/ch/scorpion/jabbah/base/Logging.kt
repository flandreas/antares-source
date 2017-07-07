package ch.scorpion.jabbah.base

import kotlin.reflect.KClass

/**
 * Defines a simple API for a logging system that is implemented on the various supported platforms.
 *
 * Usage pattern:
 *
 *      class Something {
 *          private val LOG by logger()
 *
 *          fun foo() {
 *              LOG.info("Hello from Something")
 *          }
 *      }
 */

interface LogSystem {

    /**
     * Returns the [Logger] to be used for a particular object.
     *
     * If the JVM target would be the only target to be supported, this function would have a [KClass] as
     * its only argument. Unfortunately, there is currently no pure Kotlin way to determine the [KClass] of
     * an arbitrary Kotlin object. On the Java platform, one could use "something.javaClass.kotlin", which of course
     * doesn't work in JavaScript. JetBrains says that there will be a "something::class" syntax in a future
     * release of Kotlin.
     *
     * TODO Improve with future Kotlin versions
     */
    fun getLogger(obj: Any): Lazy<Logger>
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
}

fun <T: Any> T.logger(): Lazy<Logger> {
    return LOG_SYSTEM!!.getLogger(this)
}

fun <T: Any> T.loggerFor(origin: T): Lazy<Logger> {
    return LOG_SYSTEM!!.getLogger(origin)
}



