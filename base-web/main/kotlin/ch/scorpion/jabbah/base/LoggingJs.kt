package ch.scorpion.jabbah.base

import kotlin.reflect.KClass

/**
 * Implements the [LogSystem] interface on the JavaScript platform.
 */
class LogSystemJs : LogSystem {

    private val loggers = mutableMapOf<KClass<out Any>,Lazy<Logger>>()

    private var rootLevel: LogLevel = LogLevel.INFO

    override fun getLogger(clazz: KClass<out Any>): Lazy<Logger> {
        return loggers.getOrPut(clazz, { lazy {LoggerJs()} })
    }

    private inner class LoggerJs(private var localLevel: LogLevel? = null) : Logger {

        override fun setLogLevel(level: LogLevel?) {
            localLevel = level
        }

        override fun error(msg: String) {
            if (level().ordinal >= LogLevel.ERROR.ordinal) {
                console.error(msg)
            }
        }

        override fun warn(msg: String) {
            if (level().ordinal >= LogLevel.WARN.ordinal) {
                console.warn(msg)
            }
        }

        override fun info(msg: String) {
            if (level().ordinal >= LogLevel.INFO.ordinal) {
                console.info(msg)
            }
        }

        override fun debug(msg: String) {
            if (level().ordinal >= LogLevel.DEBUG.ordinal) {
                console.log(msg)
            }
        }

        override fun trace(msg: String) {
            if (level().ordinal >= LogLevel.TRACE.ordinal) {
                console.log(msg)
            }
        }

        override fun isDebugEnabled(): Boolean {
            return level().ordinal >= LogLevel.DEBUG.ordinal
        }

        override fun isTraceEnabled(): Boolean {
            return level().ordinal >= LogLevel.TRACE.ordinal
        }

        private fun level(): LogLevel {
            return localLevel ?: rootLevel
        }
    }
}