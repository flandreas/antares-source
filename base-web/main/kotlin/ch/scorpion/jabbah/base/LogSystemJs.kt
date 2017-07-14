package ch.scorpion.jabbah.base

/**
 * Implements the [LogSystem] interface on the JavaScript platform.
 */
class LogSystemJs : LogSystem {

    val logger: Lazy<Logger> = lazy { LoggerJs() }

    override fun getLogger(obj: Any): Lazy<Logger> {
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

    var logLevel : LogLevel = LogLevel.INFO

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