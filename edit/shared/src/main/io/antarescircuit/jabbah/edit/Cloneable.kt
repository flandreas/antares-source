package io.antarescircuit.jabbah.edit

import io.antarescircuit.jabbah.io.StorableCloner
import io.antarescircuit.jabbah.io.Storable

/**
 * Implemented by objects able to create clones of itself.
 *
 * Cloning is defined in terms of deep copies of mutable inner objects. Objects with complex inner
 * objects can make use of [StorableCloner] if they and their inner objects are [Storable]s.
 */
interface Cloneable<T: Any> {

	/**
	 * Creates a clone of this object.
	 * Intentionally not named `clone` because it seems that ProGuard obfuscator would then confuse it with java.lang.Object.clone().
	 */
	fun doClone(): T
}