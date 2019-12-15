package ch.scorpion.jabbah.base

import ch.scorpion.jabbah.base.geom.AffineTransform
import ch.scorpion.jabbah.base.geom.AffineTransformJvm
import ch.scorpion.jabbah.base.geom.Path
import ch.scorpion.jabbah.base.geom.Path2DJvm
import ch.scorpion.jabbah.base.time.RealTimeTimerJvm
import ch.scorpion.jabbah.base.time.Timer
import javax.swing.SwingUtilities
import kotlin.reflect.KClass

/** Implements the [System] interface on the Java virtual machine platform.*/
class SystemJvm : System {

	/** ---- [System] interface */

	override fun createTimer(): Timer = RealTimeTimerJvm()

	override fun currentTimeMillis(): Long = java.lang.System.currentTimeMillis()

	override fun getClassName(obj: Any): String = obj.javaClass.simpleName

	override fun getClass(obj: Any): KClass<Any> = obj.javaClass.kotlin

	override fun <T : Any> instantiate(clazz: KClass<T>): T = clazz.java.getDeclaredConstructor().newInstance()

	override fun createAffineTransform(): AffineTransform = AffineTransformJvm()

	override fun createPath(): Path = Path2DJvm()

	override fun createUUID(uuid: String?): UUID {
		if (uuid == null) {
			return UUID(java.util.UUID.randomUUID().toString())
		}
		return UUID(uuid)
	}

	override fun buildToolTipText(title: String?, text: String?, endWithPeriod: Boolean): String? {
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
			if (endWithPeriod && !text!!.endsWith(".")) {
				sb.append('.')
			}
		}

		sb.append("</html>")
		return sb.toString()
	}

	override fun invokeLater(invocable: () -> Unit) {
		SwingUtilities.invokeLater { invocable.invoke() }
	}

	override fun getActionAcceleratorKey(baseName: String): String = "$baseName.accelerator"

	override fun currentLanguage(): Language {
		val code = java.lang.System.getProperty("user.language")
		if (Language.supports(code)) {
			return Language.withCode(code)
		}
		return Language.DEFAULT
	}
}