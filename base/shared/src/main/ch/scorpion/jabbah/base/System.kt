package ch.scorpion.jabbah.base

import ch.scorpion.jabbah.base.time.Timer
import ch.scorpion.jabbah.base.geom.AffineTransform
import ch.scorpion.jabbah.base.geom.Path
import kotlin.reflect.KClass

expect object System {

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

    fun buildToolTipText(title: String?, text: String?, subText: String?, endWithPeriod: Boolean = false): String?

    fun createUUID(uuid: String? = null): UUID

    fun invokeLater(invocable: () -> Unit)

    /**
     * Returns the system-dependent translation key for an Action with the given base name.
     * Example: Returns "file.action.new.accelerator" for the base name "file.action.new".
     */
    fun getActionAcceleratorKey(baseName: String): String

	/**
	 * Returns the current [Language] to be used for translating dynamic text.
	 * If the current system language isn't one of those supported by Jabbah (as defined by [Language]),
	 * this method returns the default language as of [Language.DEFAULT].
	 */
	fun currentLanguage(): Language
}
