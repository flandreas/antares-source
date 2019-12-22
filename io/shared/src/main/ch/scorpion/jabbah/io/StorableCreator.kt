package ch.scorpion.jabbah.io

import kotlin.reflect.KClass

import ch.scorpion.jabbah.base.System

/**
 * Creates instances of [Storable]s while being read by a [StoreReader].
 */
interface StorableCreator {

	/** Creates an instance of a [Storable] for the specified class.*/
	fun create(clazz: KClass<out Storable>): Storable
}

/** A [StorableCreator] that uses the [System] class to create the instance.*/
class SystemStorableCreator : StorableCreator {

	override fun create(clazz: KClass<out Storable>): Storable {
		return System.instantiate(clazz)
	}
}