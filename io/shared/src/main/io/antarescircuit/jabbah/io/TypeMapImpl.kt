package io.antarescircuit.jabbah.io

import io.antarescircuit.jabbah.base.System
import kotlin.reflect.KClass

class TypeMapImpl : TypeMap {

	/** Maps a registered type name to the factory that instantiates object of that type. */
	private val typeToStorable = mutableMapOf<String, () -> Any>()

    /** Maps a registered class to its type name.*/
    private val class2Type = mutableMapOf<KClass<out Any>, String>()

	/** Maps a [Storable] factory to its type name.*/
	private val storableToType = mutableMapOf<(Any) -> Boolean, String>()

    /** ---- [TypeMap] interface */

    override fun <T : Any> register(type: String, clazz: KClass<T>) {
		typeToStorable[type] = { System.instantiate(clazz) }
	    class2Type[clazz] = type
    }

	override fun <T : Any> register(
		type: String,
		condition: (Any) -> Boolean,
		factory: () -> T,
	) {
		typeToStorable[type] = factory
		storableToType[condition] = type
	}

	override fun <T: Any>  instantiate(type: String): T {
		val obj = typeToStorable[type]?.invoke() ?: throw NoSuchElementException("TypeMapImpl: type '$type' not found")
		@Suppress("UNCHECKED_CAST")
		return obj as T
	}

	override fun getTypeName(storable: Any): String =
		// Class-to-type mappings occur more often, so try them first
		class2Type[storable::class]
			?: storableToType.keys.firstOrNull { it.invoke(storable) }?.let { storableToType[it] }
			?: throw NoSuchElementException("TypeMapImpl: class '${storable::class.simpleName}' not found")
}