package ch.scorpion.jabbah.io

import kotlin.reflect.KClass

/**
 * Standard implementation of a [TypeMap].
 */
class TypeMapImpl : TypeMap {

    /** Maps a type name to its registered class.*/
    private val type2Class = mutableMapOf<String, KClass<out Any>>()

    /** Maps a registered class to its type name.*/
    private val class2Type = mutableMapOf<KClass<out Any>, String>()

    /** ---- [TypeMap] interface */

    override fun <T : Any> register(type: String, clazz: KClass<T>) {
	    type2Class[type] = clazz
	    class2Type[clazz] = type
    }

    override fun <T : Any> getClass(type: String): KClass<T> {
        val clazz = type2Class[type] ?: throw NoSuchElementException("TypeMapImpl: type '$type' not found")
	    @Suppress("UNCHECKED_CAST")
	    return clazz as KClass<T>
    }

    override fun getTypeName(clazz: KClass<*>): String {
        return class2Type[clazz] ?: throw NoSuchElementException("TypeMapImpl: class '${clazz.simpleName}' not found")
    }
}