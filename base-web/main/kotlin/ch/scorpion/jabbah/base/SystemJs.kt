package ch.scorpion.jabbah.base

import ch.scorpion.jabbah.base.geom.AffineTransform
import ch.scorpion.jabbah.base.geom.AffineTransformImpl
import ch.scorpion.jabbah.base.geom.Path
import ch.scorpion.jabbah.base.geom.Path2DJs
import ch.scorpion.jabbah.base.time.RealTimeTimerJs
import ch.scorpion.jabbah.base.time.Timer
import kotlin.js.Date
import kotlin.reflect.KClass

/**
 * Supports creating new instances of a [JsClass].
 * @see https://discuss.kotlinlang.org/t/creating-new-object-using-jsclass/2092
 */
fun <T: Any> JsClass<T>.newInstance(): T {
    inline fun callCtor(ctor: dynamic) = js("new ctor()")
    return callCtor(asDynamic()) as T
}

/**
 * Implements the [System] interface on the JavaScript platform.
 */
class SystemJs() : System {

    /** ---- [System] interface */

    override fun currentTimeMillis(): Long {
        return Date().getTime().toLong()
    }

    override fun createTimer(): Timer {
        return RealTimeTimerJs()
    }

    override fun getClassName(obj: Any): String {
        return obj::class.simpleName!!
    }

    override fun getClass(obj: Any): KClass<Any> {
        return obj::class as KClass<Any>
    }

    override fun <T : Any> instantiate(clazz: KClass<T>): T {
        return clazz.js.newInstance()
    }

    override fun createAffineTransform(): AffineTransform {
        return AffineTransformImpl()
    }

    override fun createPath(): Path {
        return Path2DJs()
    }

    override fun buildToolTipText(title: String?, text: String?, width: Int?): String? {
        throw UnsupportedOperationException("not implemented")
    }

    override fun createUUID(uuid: String?): UUID {
        // TODO Create an UUID with the same format as the one created by the JVM platform.
        // We don't need this before editing functionality in the brower is required.
        return UUID(Math.randomInt(10_000, 99_000).toString() + "-TEMP-UUID")
    }
}