package io.antarescircuit.jabbah.io

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotEquals
import kotlin.test.assertSame

/**
 * Unit tests for [GlobalIdentityCreator].
 */
class GlobalIdentityCreatorTest {

	@Test
	fun shouldProvideDistinctIdentitiesForEqualButDistinctStorables() {
		val creator = GlobalIdentityCreator()
		val s1 = EqualStorable("x")
		val s2 = EqualStorable("x")

		val id1 = creator.provideIdentity(s1)
		val id2 = creator.provideIdentity(s2)

		assertNotEquals(id1, id2)
		assertSame(s1, creator.getStorableWithIdentity(id1))
		assertSame(s2, creator.getStorableWithIdentity(id2))
	}

	@Test
	fun shouldProvideSequentialIdentitiesInRegistrationOrder() {
		val creator = GlobalIdentityCreator()
		val s1 = EqualStorable("a")
		val s2 = EqualStorable("b")

		assertEquals(0, creator.provideIdentity(s1))
		assertEquals(1, creator.provideIdentity(s2))
		assertEquals(0, creator.provideIdentity(s1))
		assertEquals(1, creator.getIdentity(s2))
	}

	@Test
	fun shouldNotFindIdentityOfUnregisteredEqualStorable() {
		val creator = GlobalIdentityCreator()
		creator.provideIdentity(EqualStorable("x"))

		assertFailsWith<IllegalArgumentException> { creator.getIdentity(EqualStorable("x")) }
	}

	/** A [Storable] that is equal to any other instance with the same [value]. */
	private class EqualStorable(val value: String) : AbstractStorable() {

		override fun equals(other: Any?): Boolean = other is EqualStorable && other.value == value

		override fun hashCode(): Int = value.hashCode()

		override fun resolve(reference: Reference, referenceResolver: ReferenceResolver) {}

		override fun write(writer: StoreWriter) {}

		override fun read(reader: StoreReader) {}
	}
}
