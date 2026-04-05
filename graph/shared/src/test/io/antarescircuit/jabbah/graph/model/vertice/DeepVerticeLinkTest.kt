package io.antarescircuit.jabbah.graph.model.vertice

import kotlin.test.*

/** Unit tests for [DeepVerticeLink].*/
class DeepVerticeLinkTest {

	@Test
	fun shouldParseSomeStoreFormat() {
		val link = DeepVerticeLink.fromStoreFormat("1/2/3")
		assertEquals(3, link.size)
		assertEquals(1, link.first)
		assertEquals(3, link.last)
	}

	@Test
	fun shouldParseEmptyStoreFormat() {
		val link = DeepVerticeLink.fromStoreFormat("")
		assertTrue(link.empty)
	}

	@Test
	fun shouldGenerateStoreFormat() {
		val link = DeepVerticeLink(listOf(1, 2, 3))
		assertEquals("1/2/3", DeepVerticeLink.toStoreFormat(link))
	}

	@Test
	fun shouldGenerateEmptyStoreFormat() {
		val link = DeepVerticeLink(listOf())
		assertEquals("", link.toStoreFormat())
	}

	@Test
	fun shouldDuplicateWithoutFirst() {
		val link = DeepVerticeLink(listOf(1, 2, 3))
		val withoutFirst = link.withoutFirst()
		assertEquals(2, withoutFirst.size)
		assertEquals(2, withoutFirst.first)
		assertEquals(3, withoutFirst.last)
	}

	@Test
	fun shouldConstructWithSingleId() {
		val link = DeepVerticeLink(42)
		assertEquals(1, link.size)
		assertEquals(42, link.first)
		assertEquals(42, link.last)
	}

	@Test
	fun shouldMakeCopyOfIds() {
		val ids = mutableListOf(1, 2, 3)
		val link = DeepVerticeLink(ids)
		ids.clear()
		assertEquals(3, link.size)
	}

	@Test
	fun shouldAppend() {
		val link = DeepVerticeLink(listOf(1, 2, 3))
		val newLink = link.append(4)
		assertEquals(4, newLink.size)
		assertEquals(4, newLink.last)
		assertNotSame(link, newLink)
	}

	@Test
	fun shouldBeEqual() {
		val link1 = DeepVerticeLink(listOf(1, 2, 3))
		val link2 = DeepVerticeLink(listOf(1, 2, 3))
		val link3 = DeepVerticeLink(listOf(1, 2, 4))

		assertEquals(link1, link2)
		assertNotEquals(link1, link3)
	}
}