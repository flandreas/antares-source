package ch.scorpion.jabbah.base

import ch.scorpion.jabbah.base.geom.*
import ch.scorpion.jabbah.base.time.RealTimeTimerJvm
import ch.scorpion.jabbah.base.time.Timer
import javafx.application.Platform
import javax.swing.SwingUtilities
import kotlin.reflect.KClass

/** Implements the [System] interface on the Java virtual machine platform.*/
class SystemJvm(private val useJavaFX: Boolean = false) : System {

    /** ---- [System] interface */
    
    override fun createTimer(): Timer {
        return RealTimeTimerJvm()
    }

    override fun currentTimeMillis(): Long {
        return java.lang.System.currentTimeMillis()
    }

    override fun getClassName(obj: Any): String {
        return obj.javaClass.simpleName
    }

    override fun getClass(obj: Any): KClass<Any> {
        return obj.javaClass.kotlin
    }

    override fun <T : Any> instantiate(clazz: KClass<T>): T {
        return clazz.java.getDeclaredConstructor().newInstance()
    }
    
    override fun createAffineTransform(): AffineTransform {
        if (useJavaFX) {
            return AffineTransformFx()
        }
        return AffineTransformJvm()
    }
    
    override fun createPath(): Path {
        if (useJavaFX) {
            return Path2DFx()
        }
        return Path2DJvm()
    }

    override fun createUUID(uuid: String?): UUID {
        if (uuid == null) {
            return UUID(java.util.UUID.randomUUID().toString())
        }
        return UUID(uuid)
    }

    override fun buildToolTipText(title: String?, text: String?): String? {
        val sb = StringBuilder()

        if (title == null || "" == title) {
            return null
        }

        val hasText = !(text == null || "" == text)

        sb.append("<html>")

        sb.append("<strong>")

        sb.append(title)
        if (hasText) {
            sb.append(":&nbsp;")
        }
        sb.append("</strong>")

        if (hasText) {
            sb.append(text)
        }

        sb.append("</html>")
        return sb.toString()
    }

    override fun invokeLater(invocable: () -> Unit) {
        if (useJavaFX) {
            Platform.runLater(invocable)
        } else {
            SwingUtilities.invokeLater { invocable.invoke() }
        }
    }

    override fun getActionAcceleratorKey(baseName: String): String {
        if (useJavaFX) {
            return "$baseName.accelerator.fx"
        }
        return "$baseName.accelerator"
    }

	override fun currentLanguage(): Language {
		val code = java.lang.System.getProperty("user.language")
		if (Language.supports(code)) {
			return Language.withCode(code)
		}
		return Language.DEFAULT
	}
}