package ch.scorpion.jabbah.io

import kotlin.reflect.KClass

/**
 * A [TypeMap] defines a bidirectional mapping between [Storable] type names and
 * a factory that can instantiate these [Storable]s.
 *
 * [TypeMap]s are used by [StoreReader] and [StoreWriter] when reading and writing [Storable]s
 * from and to persistent storage. Instead of storing the fully qualified class names of [Storable]s, their
 * registered short and unique type name is stored. Besides the advantage of shorter persistent representations,
 * the usage of [TypeMap]s allows developers to refactor source code without worrying to break existing persistent
 * representations.
 *
 * The types in the method signatures are intentionally not restricted to [Storable],
 * because this potentially allows this class to be used for other classes than [Storable]s.
 */
interface TypeMap {

    /** Associates the specified [type] name with its corresponding class.*/
    fun <T: Any> register(type: String, clazz: KClass<T>)

	/**
	 * Associates the specified [type] name (given [condition] is `true` with the [factory]
	 * that instantiates an object of that type.
	 */
	fun <T : Any> register(
		type: String,
		condition: (Any) -> Boolean,
		factory: () -> T,
	)

    /** Returns a new instance of an object with the given [type] name. */
	fun <T: Any> instantiate(type: String): T

	/** Returns the type name for [storable]. */
	fun getTypeName(storable: Any): String
}