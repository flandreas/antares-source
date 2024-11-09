package ch.scorpion.jabbah.base

import ch.scorpion.jabbah.base.LogLevel.*
import ch.scorpion.jabbah.base.LogSystem.PROP_LOG_LEVEL
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.base.preferences.AbstractPreference
import ch.scorpion.jabbah.base.preferences.PreferencesPanel
import org.apache.log4j.Logger.getRootLogger
import org.slf4j.LoggerFactory
import org.slf4j.spi.LocationAwareLogger
import javax.swing.JComboBox
import kotlin.reflect.KClass
import kotlin.reflect.full.companionObject

/**
 * Bridges the logging system to slf4j.
 */
actual object LogSystem {

	private val LOG: Logger = Logger(LoggerFactory.getLogger(LogSystem::class.simpleName))

	actual val PROP_LOG_LEVEL = "base.logLevel"

	actual var level: LogLevel = Info
		set(value) {
			field = value
			if (value != fromLog4jLevel(getRootLogger().level)) {
				LOG.info("Setting log level to $value")
				getRootLogger().level = toLog4jLevel(value)
				BaseModule.properties.customize(PROP_LOG_LEVEL, value.name)
			}
		}

	actual fun getLogger(clazz: KClass<out Any>): Lazy<Logger> {
		return lazy { Logger(LoggerFactory.getLogger(unwrapCompanionClass(clazz.java).name)) }
	}

	/** Unwrap companion class to enclosing class given a Java class.*/
	private fun <T : Any> unwrapCompanionClass(ofClass: Class<T>): Class<*> {
		return if (ofClass.enclosingClass != null && ofClass.enclosingClass.kotlin.companionObject?.java == ofClass) {
			ofClass.enclosingClass
		} else {
			ofClass
		}
	}

	fun toLog4jLevel(level: LogLevel): org.apache.log4j.Level {
		return when (level) {
			Error -> org.apache.log4j.Level.ERROR
			Warning -> org.apache.log4j.Level.WARN
			Info -> org.apache.log4j.Level.INFO
			Debug -> org.apache.log4j.Level.DEBUG
			Trace -> org.apache.log4j.Level.TRACE
		}
	}

	fun fromLog4jLevel(level: org.apache.log4j.Level): LogLevel {
		return when (level) {
			org.apache.log4j.Level.ERROR -> Error
			org.apache.log4j.Level.WARN -> Warning
			org.apache.log4j.Level.INFO -> Info
			org.apache.log4j.Level.DEBUG -> Debug
			org.apache.log4j.Level.TRACE -> Trace
			else -> throw IllegalArgumentException("unknown log level $level")
		}
	}
}

actual class Logger(private val slf4jLogger: org.slf4j.Logger) {

	private val fqcn = Logger::class.java.name

	// Not yet implemented
	actual var level: LogLevel?
		get() = null
		set(value) {
			// empty
		}

	actual fun userTrail(msg: String) {
		info(msg)
		UserActionTrail.add(msg)
	}

	actual fun info(msg: String) {
		if (slf4jLogger is LocationAwareLogger) {
			slf4jLogger.log(null, fqcn, LocationAwareLogger.INFO_INT, msg, null, null)
		} else {
			slf4jLogger.info(msg)
		}
	}

	actual fun warn(msg: String) {
		if (slf4jLogger is LocationAwareLogger) {
			slf4jLogger.log(null, fqcn, LocationAwareLogger.WARN_INT, msg, null, null)
		} else {
			slf4jLogger.warn(msg)
		}
	}

	actual fun error(msg: String, t: Throwable?) {
		if (slf4jLogger is LocationAwareLogger) {
			slf4jLogger.log(null, fqcn, LocationAwareLogger.ERROR_INT, msg, null, t)
		} else {
			slf4jLogger.error(msg)
		}
	}

	actual fun debug(msg: String) {
		if (slf4jLogger is LocationAwareLogger) {
			slf4jLogger.log(null, fqcn, LocationAwareLogger.DEBUG_INT, msg, null, null)
		} else {
			slf4jLogger.debug(msg)
		}
	}

	actual fun trace(msg: String) {
		if (slf4jLogger is LocationAwareLogger) {
			slf4jLogger.log(null, fqcn, LocationAwareLogger.TRACE_INT, msg, null, null)
		} else {
			slf4jLogger.trace(msg)
		}
	}

	actual fun isTraceEnabled(): Boolean = slf4jLogger.isTraceEnabled

	actual fun isDebugEnabled(): Boolean = slf4jLogger.isDebugEnabled
}

class LogLevelPreference(
	eventBus: EventBus = BaseModule.eventBus
) : AbstractPreference(id = PROP_LOG_LEVEL, nameKey = "base.preferences.logLevel") {

	private val editor = JComboBox<LogLevel>()

	private val value: LogLevel get() = valueOf(panel!!.preferences.getString(id))

	init {
		values().forEach { editor.addItem(it) }
		editor.addActionListener {
			if (panel != null) {
				panel?.preferences?.customize(this, (editor.selectedItem as LogLevel).name)
			}
		}
		registerEditor(editor)
		eventBus.register(PreferencesChangedEvent::class) { LogSystem.level = valueOf(BaseModule.properties.getString(PROP_LOG_LEVEL)) }
	}

	override fun addToPanel(panel: PreferencesPanel) {
		this.panel = panel
		panel.addLabeledRow(name, editor)
	}

	override fun load() {
		editor.selectedItem = value
	}
}
