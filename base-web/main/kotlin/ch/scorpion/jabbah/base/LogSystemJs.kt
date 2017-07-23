package ch.scorpion.jabbah.base

import kotlin.reflect.KClass

/**
 * Implements the [LogSystem] interface on the JavaScript platform.
 */
class LogSystemJs : LogSystem {

    val logger: Lazy<Logger> = lazy { LoggerJs() }

    override fun getLogger(clazz: KClass<out Any>): Lazy<Logger> {
        return logger
    }

    enum class LogLevel {
        NONE,
        ERROR,
        WARN,
        INFO,
        DEBUG,
        TRACE
    }

    var logLevel : LogLevel = LogLevel.DEBUG

    private inner class LoggerJs : Logger {

        override fun error(msg: String) {
            if (logLevel.ordinal >= LogLevel.ERROR.ordinal) {
                console.error(msg)
            }
        }

        override fun warn(msg: String) {
            if (logLevel.ordinal >= LogLevel.WARN.ordinal) {
                console.warn(msg)
            }
        }

        override fun info(msg: String) {
            if (logLevel.ordinal >= LogLevel.INFO.ordinal) {
                console.info(msg)
            }
        }

        override fun debug(msg: String) {
            if (logLevel.ordinal >= LogLevel.DEBUG.ordinal) {
                console.log(msg)
            }
        }

        override fun trace(msg: String) {
            if (logLevel.ordinal >= LogLevel.TRACE.ordinal) {
                console.log(msg)
            }
        }

        override fun isDebugEnabled(): Boolean {
            return logLevel.ordinal >= LogLevel.DEBUG.ordinal
        }

        override fun isTraceEnabled(): Boolean {
            return logLevel.ordinal >= LogLevel.TRACE.ordinal
        }
    }
}