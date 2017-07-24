package ch.scorpion.jabbah.base

import org.slf4j.LoggerFactory
import kotlin.reflect.KClass
import kotlin.reflect.companionObject

/**
 * Bridges the logging system to slf4j.
 */
class LogSystemJVM : LogSystem {

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
}

class LoggerJvm(val slf4jLogger: org.slf4j.Logger) : Logger {
    override fun info(msg: String) = slf4jLogger.info(msg)
    override fun warn(msg: String) = slf4jLogger.warn(msg)
    override fun error(msg: String) = slf4jLogger.error(msg)
    override fun debug(msg: String) = slf4jLogger.debug(msg)
    override fun trace(msg: String) = slf4jLogger.trace(msg)
    override fun isTraceEnabled(): Boolean = slf4jLogger.isTraceEnabled
    override fun isDebugEnabled(): Boolean = slf4jLogger.isDebugEnabled

    override fun setLogLevel(level: LogLevel?) {
        throw UnsupportedOperationException("LogSystemJVM.setLogLevel) not supported for slf4j")
    }
}
