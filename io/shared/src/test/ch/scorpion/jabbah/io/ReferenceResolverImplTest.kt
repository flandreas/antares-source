package ch.scorpion.jabbah.io

import ch.scorpion.jabbah.base.collection.EmptyIterator
import ch.scorpion.jabbah.base.module.BaseModule
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.BeforeTest

/**
 * Unit tests for [ReferenceResolverImpl].
 */
class ReferenceResolverImplTest {

    private var result = mutableListOf<Int>()

	@BeforeTest
	fun setup() {
		BaseModule.require()
	}

    @Test
    fun shouldSortResolutionRequestsTopologicly() {
        val rr = ReferenceResolverImpl()

        val s1 = TestStorable()
        val s2 = TestStorable()
        val s3 = TestStorable()
        val s4 = TestStorable()

        rr.addStorable(1, s1)
        rr.addStorable(2, s2)
        rr.addStorable(3, s3)
        rr.addStorable(4, s4)

        rr.requestResolution(s1, Reference(resolveAfter = listOf(2)))
        rr.requestResolution(s2, Reference(resolveAfter = listOf(4)))
        rr.requestResolution(s3, Reference())
        rr.requestResolution(s4, Reference(resolveAfter = listOf(3)))

        result = mutableListOf()
        rr.resolveReferences()

        assertEquals(result.size, 4)

        assertEquals(3, result[0])
        assertEquals(4, result[1])
        assertEquals(2, result[2])
        assertEquals(1, result[3])
    }

    @Test
    fun shouldSortResolutionRequestsTopologiclyWithDanglingStorables() {
        val rr = ReferenceResolverImpl()

        val s1 = TestStorable()
        val s2 = TestStorable()
        val s3 = TestStorable()
        val s4 = TestStorable()

        rr.addStorable(1, s1)
        rr.addStorable(2, s2)
        rr.addStorable(3, s3)
        rr.addStorable(4, s4)

        rr.requestResolution(s1, Reference(resolveAfter = listOf(2)))
        rr.requestResolution(s2, Reference(resolveAfter = listOf(4)))
        rr.requestResolution(s4, Reference(resolveAfter = listOf(3)))

        result = mutableListOf()
        rr.resolveReferences()

        assertEquals(3, result.size)

        assertEquals(4, result[0])
        assertEquals(2, result[1])
        assertEquals(1, result[2])
    }

    private inner class TestStorable : Storable {

        override var storableId: Int = 0

        override fun resolve(reference: Reference, referenceResolver: ReferenceResolver) {
            result.add(storableId)
        }

        override fun write(writer: StoreWriter) {
            // empty
        }

        override fun read(reader: StoreReader) {
            // empty
        }

        override fun getStorableChildren(): Iterator<Storable> {
            return EmptyIterator()
        }
    }
}