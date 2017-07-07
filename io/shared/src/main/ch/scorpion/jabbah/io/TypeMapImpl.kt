package ch.scorpion.jabbah.io

import ch.scorpion.jabbah.base.exception.NoSuchElementException
import ch.scorpion.jabbah.base.logger
import kotlin.reflect.KClass

/**
 * Standard implementation of a [TypeMap].
 */
class TypeMapImpl : TypeMap {

    private val LOG by logger()

    /** Maps a type name to its registered class.*/
    private val type2Class = mutableMapOf<String, KClass<out Any>>()

    /** Maps a registered class to its type name.*/
    private val class2Type = mutableMapOf<KClass<out Any>, String>()

    /** ---- [TypeMap] interface */

    override fun <T : Any> register(type: String, clazz: KClass<T>) {
        if (type2Class.containsKey(type)) {
            LOG.warn("TypeMapImpl already contains registration for $type. Replacing.")
        }
        type2Class.put(type, clazz)
        class2Type.put(clazz, type)
    }

    override fun <T : Any> getClass(type: String): KClass<T> {
        val clazz = type2Class.get(type) ?: throw NoSuchElementException("TypeMapImpl: type '$type' not found")
        return clazz as KClass<T>
    }

    override fun getTypeName(clazz: KClass<Any>): String {
        return class2Type.get(clazz) ?: throw NoSuchElementException("TypeMapImpl: class '${clazz.simpleName}' not found")
    }
}