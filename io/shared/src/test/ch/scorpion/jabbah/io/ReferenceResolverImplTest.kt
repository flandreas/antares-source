package ch.scorpion.jabbah.io

import org.hamcrest.CoreMatchers.`is`
import org.junit.Assert.*
import org.junit.Test
import java.util.Collections.emptyIterator

/**
 * Unit tests for [ReferenceResolverImpl].
 */
class ReferenceResolverImplTest {

    private var result = mutableListOf<Int>()

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

        assertThat(result.size, `is` (4))

        assertThat(result[0], `is`(3))
        assertThat(result[1], `is`(4))
        assertThat(result[2], `is`(2))
        assertThat(result[3], `is`(1))
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

        assertThat(result.size, `is`(3))

        assertThat(result[0], `is`(4))
        assertThat(result[1], `is`(2))
        assertThat(result[2], `is`(1))
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
            return emptyIterator()
        }
    }
}