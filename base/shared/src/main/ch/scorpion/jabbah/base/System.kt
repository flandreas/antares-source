package ch.scorpion.jabbah.base

import ch.scorpion.jabbah.base.time.Timer
import ch.scorpion.jabbah.base.geom.AffineTransform
import ch.scorpion.jabbah.base.geom.Path
import kotlin.reflect.KClass

expect object System {

	/**
	 * Invokes the specified invocable on the system's event queue.
	 * Can be replaced for testing purposed with an implementation that invokes the invocable immediately.
	 */
	var invoker: (() -> Unit) -> Unit

    /** Returns the current system time in milliseconds.*/
    fun currentTimeMillis(): Long

    /** Creates a new [Timer].*/
    fun createTimer(): Timer

	fun getClassName(clazz: KClass<*>): String

    /** Returns the class name of an object.*/
    fun getClassName(obj: Any): String

    fun getClass(obj: Any): KClass<*>

	/** Returns the nearest common superclass of all [classes] (excluding interfaces). */
	fun commonSuperClass(classes: Collection<KClass<*>>): KClass<*>?

    /** Creates an instance of the specified class by calling its parameterless constructor. */
    fun <T: Any> instantiate(clazz: KClass<T>): T
    
    /** Creates a new [AffineTransform].*/
    fun createAffineTransform(): AffineTransform
    
    /** Creates a new [Path].*/
    fun createPath(): Path

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

	/**
	 * Opens the specified URL in a browser.
	 * @param url the URL to be opened
	 * @param actionName the name of the action that requests browsing. Used as title in an info dialog
	 * in case the URL is not found.
	 */
	fun browse(url: String, actionName: String)

	fun printStackTrace()

	/**
	 * Used for setting breakpoints in the JVM implementation as workaround for IntelliJ's
	 * multiplatform bug with setting breakpoints in common code.*/
	fun breakpoint(condition: () -> Boolean = { true })

	/**
	 * Returns the contents of a file with the specified [path].
	 * Environments without access to files can return `null`.
	 */
	fun getFileContents(path: String): String?
}

