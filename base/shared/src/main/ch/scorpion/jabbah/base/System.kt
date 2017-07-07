package ch.scorpion.jabbah.base

import ch.scorpion.jabbah.base.time.Timer
import ch.scorpion.jabbah.base.geom.AffineTransform
import ch.scorpion.jabbah.base.geom.Path
import kotlin.reflect.KClass

interface System {

    companion object {
        var SYSTEM: System? = null

        // Used for calling in constructors of classes (Bug in JS-Kotlin 1.0)
        fun get(): System {
            return SYSTEM!!
        }
    }
    
    /** Returns the current system time in milliseconds.*/
    fun currentTimeMillis(): Long

    /** Creates a new [Timer].*/
    fun createTimer(): Timer

    /** Returns the simple class name of an object.*/
    fun getClassName(obj: Any): String

    fun getClass(obj: Any): KClass<Any>

    /** Creates an instance of the specified class by calling its parameterless constructor. */
    fun <T: Any> instantiate(clazz: KClass<T>): T
    
    /** Creates a new [AffineTransform].*/
    fun createAffineTransform(): AffineTransform
    
    /** Creates a new [Path].*/
    fun createPath(): Path

    fun buildToolTipText(title: String?, text: String?, width: Int?): String?

    fun createUUID(uuid: String? = null): UUID

}
