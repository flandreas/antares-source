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
	inline fun callCtor(@Suppress("UNUSED_PARAMETER") ctor: dynamic) = js("new ctor()")
	return callCtor(asDynamic())
}

/**
 * Implements the [System] interface on the JavaScript platform.
 */
actual object System {

	/** ---- [System] interface */

	actual var invoker: (() -> Unit) -> Unit = { it.invoke() }

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

	actual fun getClass(obj: Any): KClass<*> {
		return obj::class
	}

	/** Not yet implemented on the JS platform. */
	actual fun commonSuperClass(classes: Collection<KClass<*>>): KClass<*>? = null

	actual fun <T : Any> instantiate(clazz: KClass<T>): T {
		return clazz.js.newInstance()
	}

	actual fun createAffineTransform(): AffineTransform {
		return AffineTransformImpl()
	}

	actual fun createPath(): Path {
		return Path2DJs()
	}

	actual fun createUUID(uuid: String?): UUID {
		// TODO Create an UUID with the same format as the one created by the JVM platform.
		// We don't need this before editing functionality in the browser is required.
		return uuid?.let { UUID(uuid) } ?: UUID(Random.nextInt(10_000, 99_000).toString() + "-TEMP-UUID")
	}

	actual fun invokeLater(invocable: () -> Unit) {
		invoker.invoke(invocable)
	}

	actual fun getActionAcceleratorKey(baseName: String): String = "$baseName.accelerator"

	actual fun currentLanguage(): Language = Language.English

	actual fun browse(url: String, actionName: String) {
		throw NotImplementedError("System.browse() not implemented")
	}

	actual fun printStackTrace() {
		// empty
	}

	actual fun breakpoint(condition: () -> Boolean) {
		// empty
	}
}