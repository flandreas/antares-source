package io.antarescircuit.jabbah.base.collection

import io.antarescircuit.jabbah.base.System

/**
 * Wraps [value] so that it is compared by identity (`===`) when used as a key of hash-based collections,
 * regardless of whether [value] overrides [Any.equals] and [Any.hashCode].
 */
class IdentityKey<T : Any>(val value: T) {

	override fun equals(other: Any?): Boolean = other is IdentityKey<*> && other.value === value

	override fun hashCode(): Int = System.identityHashCode(value)
}
