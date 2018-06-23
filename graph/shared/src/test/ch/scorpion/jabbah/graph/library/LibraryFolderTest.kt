package ch.scorpion.jabbah.graph.library

import org.hamcrest.CoreMatchers.*
import org.junit.Assert.*
import org.junit.ClassRule
import org.junit.Test

/** Unit tests for [LibraryFolder]. */
class LibraryFolderTest {

	companion object {
		@ClassRule
		@JvmField
		val rule = GraphLibraryTestRule()
	}

	@Test
	fun shouldContainDirectly() {
		val folder = LibraryFolder("test")
		val item = LibraryFolder("item")
		val item2 = LibraryFolder("item2")
		folder.add(item)

		assertThat(folder.contains(item), `is`(true))
		assertThat(folder.contains(item2), `is`(false))
	}

	@Test
	fun shouldContainRecursively() {
		val folder = LibraryFolder("test")
		val folder2 = LibraryFolder("folder2")
		val item = LibraryFolder("item")
		folder.add(folder2)
		folder2.add(item)

		assertThat(folder.contains(item), `is`(false))
		assertThat(folder.containsRecursively(item), `is`(true))
	}

	@Test
	fun shouldGetDirectly() {
		val folder = LibraryFolder("test")
		val item = LibraryFolder("item")
		val item2 = LibraryFolder("item2")

		folder.add(item)
		assertThat(folder.get("item") as LibraryFolder, sameInstance(item))
	}

	@Test
	fun shouldGetRecursively() {
		val folder = LibraryFolder("test")
		val folder2 = LibraryFolder("folder2")
		val item = LibraryFolder("item")
		folder.add(folder2)
		folder2.add(item)

		assertThat(folder.getRecursively("item") as LibraryFolder?, sameInstance(item))
		assertThat(folder.getRecursively("bla") as LibraryFolder?, `is`(nullValue()));
	}
}