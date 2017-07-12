package ch.scorpion.jabbah.base

import ch.scorpion.jabbah.base.geom.*
import ch.scorpion.jabbah.base.time.RealTimeTimerJvm
import ch.scorpion.jabbah.base.time.Timer
import kotlin.reflect.KClass

/** Implements the [System] interface on the Java virtual machine platform.*/
class SystemJvm : System {

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
        return clazz.java.newInstance()
    }
    
    override fun createAffineTransform(): AffineTransform {
        return AffineTransformJvm()
    }
    
    override fun createPath(): Path {
        return Path2DJvm()
    }

    override fun createUUID(uuid: String?): UUID {
        if (uuid == null) {
            return UUID(java.util.UUID.randomUUID().toString())
        }
        return UUID(uuid)
    }

    override fun buildToolTipText(title: String?, text: String?, width: Int?): String? {
        val sb = StringBuilder()

        if (title == null || "" == title) {
            return null
        }

        val hasText = !(text == null || "" == text)

        sb.append("<html>")

        if (width != null && hasText) {
            sb.append("<div style=\"width: " + width.toString() + "px; text-justification: justify;\">")
        }

        sb.append("<strong>")

        sb.append(title)
        if (hasText) {
            sb.append(":&nbsp;")
        }
        sb.append("</strong>")

        if (hasText) {
            sb.append(text)
        }

        if (width != null && hasText) {
            sb.append("</div>")
        }

        sb.append("</html>")
        return sb.toString()
    }
}