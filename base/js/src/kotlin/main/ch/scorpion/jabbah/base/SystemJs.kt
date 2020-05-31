package ch.scorpion.jabbah.base

import ch.scorpion.jabbah.base.geom.AffineTransform
import ch.scorpion.jabbah.base.geom.AffineTransformImpl
import ch.scorpion.jabbah.base.geom.Path
import ch.scorpion.jabbah.base.geom.Path2DJs
import ch.scorpion.jabbah.base.time.RealTimeTimerJs
import ch.scorpion.jabbah.base.time.Timer
import kotlin.js.Date
import kotlin.random.Random
import kotlin.reflect.KClass

/**
 * Supports creating new instances of a [JsClass].
 * @see https://discuss.kotlinlang.org/t/creating-new-object-using-jsclass/2092
 */
fun <T : Any> JsClass<T>.newInstance(): T {
	inline fun callCtor(ctor: dynamic) = js("new ctor()")
	return callCtor(asDynamic())
}

/**
 * Implements the [System] interface on the JavaScript platform.
 */
actual object System {

	/** ---- [System] interface */

	actual fun currentTimeMillis(): Long {
		return Date().getTime().toLong()
	}

	actual fun createTimer(): Timer {
		return RealTimeTimerJs()
	}

	actual fun getClassName(clazz: KClass<*>): String = clazz.simpleName!!

	actual fun getClassName(obj: Any): String {
		return obj::class.simpleName!!
	}

	actual fun getClass(obj: Any): KClass<Any> {
		return obj::class as KClass<Any>
	}

	actual fun <T : Any> instantiate(clazz: KClass<T>): T {
		return clazz.js.newInstance()
	}

	actual fun createAffineTransform(): AffineTransform {
		return AffineTransformImpl()
	}

	actual fun createPath(): Path {
		return Path2DJs()
	}

	actual fun buildToolTipText(title: String?, text: String?, subText: String?, endWithPeriod: Boolean): String? {
		// TODO Improve formatting
		val sb = StringBuilder("")
		if (StringUtils.isNotEmpty(title)) {
			sb.append(title)
			sb.append(": ")
		}
		if (StringUtils.isNotEmpty(text)) {
			sb.append(text)
		}
		if (StringUtils.isNotEmpty(subText)) {
			if (sb.isNotEmpty()) {
				sb.append("\n\n")
				sb.append(subText)
			}
		}
		return sb.toString()
	}

	actual fun createUUID(uuid: String?): UUID {
		// TODO Create an UUID with the same format as the one created by the JVM platform.
		// We don't need this before editing functionality in the brower is required.
		return UUID(Random.nextInt(10_000, 99_000).toString() + "-TEMP-UUID")
	}

	actual fun invokeLater(invocable: () -> Unit) {
		invocable.invoke()
	}

	actual fun getActionAcceleratorKey(baseName: String): String = "$baseName.accelerator"

	actual fun currentLanguage(): Language = Language.English

	actual fun printStackTrace() {
		// empty
	}
}