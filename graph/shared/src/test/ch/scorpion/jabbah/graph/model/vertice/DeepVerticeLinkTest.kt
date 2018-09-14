package ch.scorpion.jabbah.graph.model.vertice

import org.hamcrest.Matchers.*
import org.junit.Assert.*
import org.junit.Test

/** Unit tests for [DeepVerticeLink].*/
class DeepVerticeLinkTest {

	@Test
	fun shouldParseSomeStoreFormat() {
		val link = DeepVerticeLink.fromStoreFormat("1/2/3")
		assertThat(link.size, `is`(3))
		assertThat(link.first, `is`(1))
		assertThat(link.last, `is`(3))
	}

	@Test
	fun shouldParseEmptyStoreFormat() {
		val link = DeepVerticeLink.fromStoreFormat("")
		assertThat(link.empty, `is`(true) )
	}

	@Test
	fun shouldGenerateStoreFormat() {
		val link = DeepVerticeLink(listOf(1, 2, 3))
		assertThat(DeepVerticeLink.toStoreFormat(link), `is`("1/2/3"))
	}

	@Test
	fun shouldGenerateEmptyStoreFormat() {
		val link = DeepVerticeLink(listOf())
		assertThat(link.toStoreFormat(), `is`(""))
	}

	@Test
	fun shouldDuplicateWithoutFirst() {
		val link = DeepVerticeLink(listOf(1, 2, 3))
		val withoutFirst = link.withoutFirst()
		assertThat(withoutFirst.size, `is`(2))
		assertThat(withoutFirst.first, `is`(2))
		assertThat(withoutFirst.last, `is`(3))
	}

	@Test
	fun shouldConstructWithSingleId() {
		val link = DeepVerticeLink(42)
		assertThat(link.size, `is`(1))
		assertThat(link.first, `is`(42))
		assertThat(link.last, `is`(42))
	}

	@Test
	fun shouldMakeCopyOfIds() {
		val ids = mutableListOf(1, 2, 3)
		val link = DeepVerticeLink(ids)
		ids.clear()
		assertThat(link.size, `is`(3))
	}

	@Test
	fun shouldAppend() {
		val link = DeepVerticeLink(listOf(1, 2, 3))
		val newLink = link.append(4)
		assertThat(newLink.size, `is`(4))
		assertThat(newLink.last, `is`(4))
		assertThat(newLink, not(sameInstance(link)))
	}
}