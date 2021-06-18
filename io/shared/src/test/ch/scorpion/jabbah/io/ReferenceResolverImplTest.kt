package ch.scorpion.jabbah.io

import ch.scorpion.jabbah.base.module.BaseModule
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

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
    fun shouldSortResolutionRequestsTopologically() {
        val rr = ReferenceResolverImpl()

        val s1 = testStorable()
        val s2 = testStorable()
        val s3 = testStorable()
        val s4 = testStorable()

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
    fun shouldSortResolutionRequestsTopologicallyWithDanglingStorables() {
        val rr = ReferenceResolverImpl()

        val s1 = testStorable()
        val s2 = testStorable()
        val s3 = testStorable()
        val s4 = testStorable()

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

	private fun testStorable(): Storable = TestStorable { result.add(it) }
}