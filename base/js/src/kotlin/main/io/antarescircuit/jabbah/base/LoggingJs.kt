package io.antarescircuit.jabbah.base

import io.antarescircuit.jabbah.base.LogLevel.*
import kotlin.reflect.KClass

/**
 * Implements the [LogSystem] interface on the JavaScript platform.
 */
actual object LogSystem {

    private val loggers = mutableMapOf<KClass<out Any>,Lazy<Logger>>()

	actual val PROP_LOG_LEVEL = "base.logLevel"

	actual var level: LogLevel = Warning

	actual fun getLogger(clazz: KClass<out Any>): Lazy<Logger> =
		loggers.getOrPut(clazz) { lazy { Logger() } }
}

actual class Logger(actual var level: LogLevel? = null) {

	actual fun userTrail(msg: String) {
		console.info(msg)
	}

	actual fun error(msg: String, t: Throwable?) {
		if (effectiveLevel().ordinal >= Error.ordinal) {
			console.error(msg)
		}
	}

	actual fun warn(msg: String) {
		if (effectiveLevel().ordinal >= Warning.ordinal) {
			console.warn(msg)
		}
	}

	actual fun info(msg: String) {
		if (effectiveLevel().ordinal >= Info.ordinal) {
			console.info(msg)
		}
	}

	actual fun debug(msg: String) {
		if (effectiveLevel().ordinal >= Debug.ordinal) {
			console.log(msg)
		}
	}

	actual fun trace(msg: String) {
		if (effectiveLevel().ordinal >= Trace.ordinal) {
			console.log(msg)
		}
	}

	actual fun isDebugEnabled(): Boolean = effectiveLevel().ordinal >= Debug.ordinal

	actual fun isTraceEnabled(): Boolean = effectiveLevel().ordinal >= Trace.ordinal

	private fun effectiveLevel(): LogLevel = level ?: LogSystem.level
}