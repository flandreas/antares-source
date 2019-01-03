package ch.scorpion.jabbah.base

import ch.scorpion.jabbah.base.LogLevel.*
import ch.scorpion.jabbah.base.LogSystem.Companion.PROP_LOG_LEVEL
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.base.preferences.AbstractPreference
import ch.scorpion.jabbah.base.preferences.PreferencesChangedEvent
import ch.scorpion.jabbah.base.preferences.PreferencesPanel
import org.apache.log4j.Logger.*
import org.slf4j.LoggerFactory
import javax.swing.JComboBox
import kotlin.reflect.KClass
import kotlin.reflect.full.companionObject

/**
 * Bridges the logging system to slf4j.
 */
class LogSystemJVM : LogSystem {

	private val LOG: Logger = LoggerJvm(LoggerFactory.getLogger(LogSystemJVM::class.simpleName))

	private val propertyValue: LogLevel get() = valueOf(BaseModule.properties.getString(PROP_LOG_LEVEL))

	override var level: LogLevel
		get() = propertyValue
		set(value) {
			if (value != fromLog4jLevel(getRootLogger().level)) {
				LOG.info("LogSystemJVM: Setting log level to $value")
				getRootLogger().level = toLog4jLevel(value)
				BaseModule.properties.customize(PROP_LOG_LEVEL, value.name)
			}
		}

	override fun getLogger(clazz: KClass<out Any>): Lazy<Logger> {
		return lazy { LoggerJvm(LoggerFactory.getLogger(unwrapCompanionClass(clazz.java).name)) }
	}

	/** Unwrap companion class to enclosing class given a Java class.*/
    private fun <T: Any> unwrapCompanionClass(ofClass: Class<T>): Class<*> {
        return if (ofClass.enclosingClass != null && ofClass.enclosingClass.kotlin.companionObject?.java == ofClass) {
            ofClass.enclosingClass
        } else {
            ofClass
        }
    }

	private fun toLog4jLevel(level: LogLevel): org.apache.log4j.Level {
		return when (level) {
			Error -> org.apache.log4j.Level.ERROR
			Warning -> org.apache.log4j.Level.WARN
			Info -> org.apache.log4j.Level.INFO
			Debug -> org.apache.log4j.Level.DEBUG
			Trace -> org.apache.log4j.Level.TRACE
		}
	}

	private fun fromLog4jLevel(level: org.apache.log4j.Level): LogLevel {
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

class LoggerJvm(private val slf4jLogger: org.slf4j.Logger) : Logger {

    override fun info(msg: String) = slf4jLogger.info(msg)
    override fun warn(msg: String) = slf4jLogger.warn(msg)
    override fun error(msg: String) = slf4jLogger.error(msg)
    override fun debug(msg: String) = slf4jLogger.debug(msg)
    override fun trace(msg: String) = slf4jLogger.trace(msg)
    override fun isTraceEnabled(): Boolean = slf4jLogger.isTraceEnabled
    override fun isDebugEnabled(): Boolean = slf4jLogger.isDebugEnabled
}

class LogLevelPreference(
	eventBus: EventBus = BaseModule.eventBus
) : AbstractPreference(id = LogSystem.PROP_LOG_LEVEL, nameKey = "base.preferences.logLevel") {

	private val editor = JComboBox<LogLevel>()

	private val value: LogLevel get() = LogLevel.valueOf(panel!!.preferences.getString(id))

	init {
		LogLevel.values().forEach { editor.addItem(it) }
		editor.addActionListener {
			if (panel != null) {
				panel?.preferences?.customize(id, (editor.selectedItem as LogLevel).name)
			}
		}
		eventBus.register(PreferencesChangedEvent::class) { LOG_SYSTEM?.level = valueOf(BaseModule.properties.getString(PROP_LOG_LEVEL))}
	}

	override fun addToPanel(panel: PreferencesPanel) {
		this.panel = panel
		panel.addLabeledRow(name, editor)
	}

	override fun load() {
		editor.selectedItem = value
	}

}
